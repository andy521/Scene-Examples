package io.agora.scene.virtualimage.manager;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.faceunity.wrapper.faceunity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.base.internal.ThreadUtils;

public class FUManager {
    private static final String TAG = "FUManager";

    /**
     * controller.bundle：controller数据文件，用于控制和显示avatar。
     */
    public static final String BUNDLE_controller_new = "new/controller_cpp.bundle";
    public static final String BUNDLE_controller_config_new = "new/controller_config.bundle";

    /**
     * 目录assets下的 *.bundle为程序的数据文件。
     * 其中 v3.bundle：人脸识别数据文件，缺少该文件会导致系统初始化失败；
     * fxaa.bundle：3D绘制抗锯齿数据文件。加载后，会使得3D绘制效果更加平滑。
     * default_bg.bundle：背景道具，使用方法与普通道具相同。
     * cam_35mm_full_80mm.bundle：首页加载的相机动画
     * 目录effects下是我们打包签名好的道具
     */
    public static final String BUNDLE_v3 = "v3.bundle";
    public static final String BUNDLE_fxaa = "fxaa.bundle";
    public static final String BUNDLE_ai_face_processor = "ai_face_processor.bundle";

    public static double[][] skin_color;
    public static double[][] lip_color;
    public static double[][] iris_color;
    public static double[][] hair_color;
    public static double[][] beard_color;
    public static double[][] glass_frame_color;
    public static double[][] glass_color;
    public static double[][] hat_color;
    public static double[][] makeup_color;


    private Context mContext;
    private int[] mToolHandlers;
    private int mToolController = -1;

    private volatile boolean isInitialized = false;
    private volatile int mFrameId;

    private HandlerThread fuItemThread;
    private Handler fuItemHandler;

    public static final int ITEM_ARRAYS_BG = 0;
    public static final int ITEM_ARRAYS_CONTROLLER = 1;
    public static final int ITEM_ARRAYS_EFFECT = 2;
    public static final int ITEM_ARRAYS_FXAA = 3;
    public static final int ITEM_ARRAYS_COUNT = 4;
    private final int[] mItemsArray = new int[ITEM_ARRAYS_COUNT];
    private faceunity.RotatedImage mRotatedImage;

    private volatile boolean isFuItemLoading = true;
    private final Map<String, Integer> bundlesLoaded = new HashMap<>();

    public static final float[] IdentityMatrix = new float[16];
    static {
        Matrix.setIdentityM(IdentityMatrix, 0);
    }


    private static volatile FUManager INSTANCE;
    private int mControllerConfig;

    private final List<Runnable> mEventQueue = Collections.synchronizedList(new ArrayList<Runnable>());;

    public static FUManager getInstance(){
        if(INSTANCE == null){
            synchronized (FUManager.class){
                if(INSTANCE == null){
                    INSTANCE = new FUManager();
                }
            }
        }
        return INSTANCE;
    }

    private FUManager(){}

    public void init(Context ct){
        if(isInitialized){
            return;
        }
        mContext = ct.getApplicationContext();
        fuItemThread = new HandlerThread("FuItemLoad");
        fuItemThread.start();
        fuItemHandler = new Handler(fuItemThread.getLooper());
        isInitialized = true;
        runInFuItemThread(new Runnable() {
            @Override
            public void run() {
                try {
                    /**
                     * 初始化dsp设备
                     * 如果已经调用过一次了，后面再重新初始化bundle，也不需要重新再调用了。
                     */
                    String path = mContext.getApplicationInfo().nativeLibraryDir;
                    faceunity.fuHexagonInitWithPath(path);

                    //获取faceunity SDK版本信息
                    Log.i(TAG, "fu sdk version " + faceunity.fuGetVersion());

                    /**
                     * fuSetup faceunity初始化
                     * 其中 v3.bundle：人脸识别数据文件，缺少该文件会导致系统初始化失败；
                     *      authpack：用于鉴权证书内存数组。若没有,请咨询support@faceunity.com
                     * 首先调用完成后再调用其他FU API
                     */
                    InputStream v3 = mContext.getAssets().open(BUNDLE_v3);
                    byte[] v3Data = new byte[v3.available()];
                    v3.read(v3Data);
                    v3.close();
                    faceunity.fuSetup(v3Data, authpack.A());

                    // 颜色列表
                    InputStream is = mContext.getAssets().open("new/color.json");
                    byte[] itemData = new byte[is.available()];
                    is.read(itemData);
                    is.close();
                    String json = new String(itemData);
                    JSONObject jsonObject = new JSONObject(json);
                    skin_color = parseColorJson(jsonObject, "skin_color");
                    lip_color = parseColorJson(jsonObject, "lip_color");
                    iris_color = parseColorJson(jsonObject, "iris_color");
                    hair_color = parseColorJson(jsonObject, "hair_color");
                    beard_color = parseColorJson(jsonObject, "beard_color");
                    glass_frame_color = parseColorJson(jsonObject, "glass_frame_color");
                    glass_color = parseColorJson(jsonObject, "glass_color");
                    hat_color = parseColorJson(jsonObject, "hat_color");
                    makeup_color = parseColorJson(jsonObject, "makeup_color");
                    ColorPickGradient.init(skin_color);

                    loadAiModel(mContext, "ai_human_processor.bundle", faceunity.FUAITYPE_HUMAN_PROCESSOR);
                    loadAiModel(mContext, BUNDLE_ai_face_processor, faceunity.FUAITYPE_FACEPROCESSOR);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void initController() {
        runInFuItemThread(new Runnable() {
            @Override
            public void run() {
                // controller的创建
                mToolController = loadFUItem(BUNDLE_controller_new);
                mControllerConfig = loadFUItem(BUNDLE_controller_config_new);
                faceunity.fuBindItems(mToolController, new int[]{mControllerConfig});
                mItemsArray[ITEM_ARRAYS_CONTROLLER] = mToolController;
                mItemsArray[ITEM_ARRAYS_FXAA] = loadFUItem(BUNDLE_fxaa);
            }
        });
    }

    public void reset(){
        mFrameId = 0;
        isFuItemLoading = true;
        mEventQueue.clear();
        bundlesLoaded.clear();
        int toolController = mToolController;
        if(toolController >= 0){
            runInFuItemThread(new Runnable() {
                @Override
                public void run() {
                    faceunity.fuUnBindItems(toolController, new int[]{mControllerConfig});
                    faceunity.fuUnBindItems(toolController, mToolHandlers);
                    faceunity.fuDestroyItem(mControllerConfig);
                    for (int handler : mToolHandlers) {
                        faceunity.fuDestroyItem(handler);
                    }
                }
            });
        }
        mToolController = -1;

    }

    public void setDriveMode(){
        runInFuItemThread(new Runnable() {
            @Override
            public void run() {
                //设置enable_face_processor，说明启用或者关闭面部追踪，value = 1.0表示开启，value = 0.0表示关闭
                faceunity.fuItemSetParam(mToolController, "enable_face_processor", 1.0);
                faceunity.fuItemSetParam(mToolController, "target_position", new double[]{0.0, -50f, 300f});
                faceunity.fuItemSetParam(mToolController, "target_angle", 0);
                faceunity.fuItemSetParam(mToolController, "reset_all", 3);
                // 将相机与相机之间的过度动画时间设置为0
                faceunity.fuItemSetParam(mToolController, "camera_animation_transition_time", 0.0);
            }
        });
    }

    private static double[][] parseColorJson(JSONObject jsonObject, String key) throws Exception {
        JSONObject object = jsonObject.getJSONObject(key);
        List<double[]> colors = new ArrayList<>();
        for (int i = 1; object.has(String.valueOf(i)); i++) {
            JSONObject color = object.getJSONObject(String.valueOf(i));
            int r = color.getInt("r");
            int g = color.getInt("g");
            int b = color.getInt("b");
            if (color.has("intensity")) {
                double intensity = color.getDouble("intensity");
                colors.add(new double[]{r, g, b, intensity});
            } else {
                colors.add(new double[]{r, g, b});
            }
        }
        double[][] doubles = new double[colors.size()][];
        colors.toArray(doubles);
        return doubles;
    }

    public static double[] getColor(double[][] colors, double value) {
        int index = (int) value;
        if (index >= colors.length - 1) return colors[colors.length - 1];
        if (index < 0) return colors[0];
        double v = value - index;
        double[] c = Arrays.copyOf(colors[index], colors[index].length);
        c[0] = colors[index][0] + v * (colors[index + 1][0] - colors[index][0]);
        c[1] = colors[index][1] + v * (colors[index + 1][1] - colors[index][1]);
        c[2] = colors[index][2] + v * (colors[index + 1][2] - colors[index][2]);
        return c;
    }

    public static double[] getMakeupColor(double[][] colors, double value) {
        int index = (int) value;
        if (index >= colors.length - 1) {
            index = colors.length - 1;
        }
        if (index <= 0) {
            index = 0;
        }
        double[] c = Arrays.copyOf(colors[index], 3);
        return c;
    }

    /**
     * 设置美妆颜色
     *
     * @param color
     */
    public void setMakeupColor(int makeupHandleId, double[] color) {
        //设置美妆的颜色
        //美妆参数名为json结构，
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "global");
            jsonObject.put("type", "face_detail");
            jsonObject.put("param", "blend_color");
            jsonObject.put("UUID", makeupHandleId);//需要修改的美妆道具bundle handle id
        } catch (JSONException e) {
            e.printStackTrace();
        }
        double[] makeupColor = new double[color.length];
        for (int i = 0; i < color.length; i++) {
            makeupColor[i] = color[i] * 1.0 / 255;
        }
        //美妆参数值为0-1之间的RGB设置，美妆颜色原始为RGB色值(sRGB空间)，RGB/255得到传给controller的值
        //例如要替换的美妆颜色为[255,0,0], 传给controller的值为[1,0,0]
        faceunity.fuItemSetParam(mToolController, jsonObject.toString(), makeupColor);
    }

    public void showMaleImage() {
        runInFuItemThread(new Runnable() {
            @Override
            public void run() {
                if(mToolHandlers != null && mToolHandlers.length > 0){
                    faceunity.fuUnBindItems(mToolController, mToolHandlers);
                }
                // 人物支架
                int wholeBodyHandler = loadFUItem("new/camera/cam_02.bundle");

                // 背景
                int defaultBgHandler = loadFUItem("default_bg.bundle");
                int planeLeftHandler = loadFUItem("plane_shadow_left.bundle");
                int planeRightHandler = loadFUItem("plane_shadow_right.bundle");

                // 加载人物道具
                int headHandler = loadFUItem("new/head/head_1/head.bundle");
                int hairHandler = loadFUItem("new/hair/male_hair_3.bundle");
                int bodyHandler = loadFUItem("new/body/male/midBody_male3.bundle");
                int clothUpperHandler = loadFUItem("new/clothes/upper/shangyi_chenshan_3.bundle");
                int clothLowerHandler = loadFUItem("new/clothes/lower/kuzi_changku_5.bundle");
                int shoesHandler = loadFUItem("new/shoes/xiezi_tuoxie_3.bundle");
                int lipglossHandler = loadFUItem("new/makeup/lipgloss/lipgloss_1.bundle");
                int expressionHandler = loadFUItem("new/expression/ani_idle.bundle");
                //int expressionScenesHandler = loadFUItem("new/expression/scenes/2d/keting_A_mesh.bundle");
                int lightHandler = loadFUItem("new/light/light_0.6.bundle");

                mToolHandlers = new int[]{
                        wholeBodyHandler,
                        defaultBgHandler, planeLeftHandler, planeRightHandler,
                        headHandler,hairHandler,bodyHandler, clothUpperHandler, clothLowerHandler,
                        shoesHandler, lipglossHandler,
                        expressionHandler,
                        //expressionScenesHandler,
                        lightHandler
                };
                runInEventQueue(new Runnable() {
                    @Override
                    public void run() {
                        faceunity.fuBindItems(mToolController, mToolHandlers);

                        // 设置颜色
                        faceunity.fuItemSetParam(mToolController, "iris_color", getColor(iris_color, 0));
                        faceunity.fuItemSetParam(mToolController, "hair_color", getColor(hair_color, 0));
                        faceunity.fuItemSetParam(mToolController, "hair_color_intensity", getColor(hair_color, 0)[3]);
                        faceunity.fuItemSetParam(mToolController, "glass_color", getColor(glass_color, 0));
                        faceunity.fuItemSetParam(mToolController, "glass_frame_color", getColor(glass_frame_color, 0));
                        faceunity.fuItemSetParam(mToolController, "beard_color", getColor(beard_color, 0));
                        faceunity.fuItemSetParam(mToolController, "hat_color", getColor(hat_color, 0));
                        setMakeupColor(lipglossHandler, getMakeupColor(lip_color, 6));
                    }
                });

                isFuItemLoading = false;
            }
        });
    }

    public void showFemaleImage() {
        runInFuItemThread(new Runnable() {
            @Override
            public void run() {
                if(mToolHandlers != null && mToolHandlers.length > 0){
                    faceunity.fuUnBindItems(mToolController, mToolHandlers);
                }
                // 人物支架
                int wholeBodyHandler = loadFUItem("new/camera/cam_02.bundle");

                // 背景
                int defaultBgHandler = loadFUItem("default_bg.bundle");
                int planeLeftHandler = loadFUItem("plane_shadow_left.bundle");
                int planeRightHandler = loadFUItem("plane_shadow_right.bundle");

                // 加载人物道具
                int headHandler = loadFUItem("new/head/head_2/head.bundle");
                int hairHandler = loadFUItem("new/head/head_2/female_hair_21.bundle");
                int bodyHandler = loadFUItem("new/body/female/midBody_female3.bundle");
                int clothHandler = loadFUItem("new/clothes/suit/taozhuang_10.bundle");
                int shoesHandler = loadFUItem("new/shoes/xiezi_tuoxie_3.bundle");
                int lipglossHandler = loadFUItem("new/makeup/lipgloss/lipgloss_1.bundle");
                int expressionHandler = loadFUItem("new/expression/ani_idle.bundle");
                //int expressionScenesHandler = loadFUItem("new/expression/scenes/2d/keting_A_mesh.bundle");
                int lightHandler = loadFUItem("new/light/light_0.6.bundle");

                mToolHandlers = new int[]{
                        wholeBodyHandler,
                        defaultBgHandler, planeLeftHandler, planeRightHandler,
                        headHandler,hairHandler,bodyHandler, clothHandler,
                        shoesHandler, lipglossHandler,
                        expressionHandler,
                        //expressionScenesHandler,
                        lightHandler
                };
                runInEventQueue(new Runnable() {
                    @Override
                    public void run() {
                        faceunity.fuBindItems(mToolController, mToolHandlers);

                        // 设置颜色
                        faceunity.fuItemSetParam(mToolController, "iris_color", getColor(iris_color, 0));
                        faceunity.fuItemSetParam(mToolController, "hair_color", getColor(hair_color, 0));
                        faceunity.fuItemSetParam(mToolController, "hair_color_intensity", getColor(hair_color, 0)[3]);
                        faceunity.fuItemSetParam(mToolController, "glass_color", getColor(glass_color, 0));
                        faceunity.fuItemSetParam(mToolController, "glass_frame_color", getColor(glass_frame_color, 0));
                        faceunity.fuItemSetParam(mToolController, "beard_color", getColor(beard_color, 0));
                        faceunity.fuItemSetParam(mToolController, "hat_color", getColor(hat_color, 0));
                        setMakeupColor(lipglossHandler, getMakeupColor(lip_color, 6));
                    }
                });
                isFuItemLoading = false;
            }
        });
    }



    /**
     * 加载 AI 模型资源
     *
     * @param context
     * @param bundlePath ai_model.bundle
     * @param type       faceunity.FUAITYPE_XXX
     */
    private static void loadAiModel(Context context, String bundlePath, int type) {
        byte[] buffer = readFile(context, bundlePath);
        if (buffer != null) {
            int isLoaded = faceunity.fuLoadAIModelFromPackage(buffer, type);
            Log.d(TAG, "loadAiModel. type: " + type + ", isLoaded: " + (isLoaded == 1 ? "yes" : "no"));
        }
    }

    /**
     * 从 assets 文件夹或者本地磁盘读文件
     *
     * @param context
     * @param path
     * @return
     */
    private static byte[] readFile(Context context, String path) {
        InputStream is = null;
        try {
            is = context.getAssets().open(path);
        } catch (IOException e1) {
            Log.w(TAG, "readFile: e1", e1);
            // open assets failed, then try sdcard
            try {
                is = new FileInputStream(path);
            } catch (IOException e2) {
                Log.w(TAG, "readFile: e2", e2);
            }
        }
        if (is != null) {
            try {
                byte[] buffer = new byte[is.available()];
                int length = is.read(buffer);
                Log.v(TAG, "readFile. path: " + path + ", length: " + length + " Byte");
                is.close();
                return buffer;
            } catch (IOException e3) {
                Log.e(TAG, "readFile: e3", e3);
            }
        }
        return null;
    }

    /**
     * 通过道具文件路径创建道具：
     *
     * @param bundle 道具文件路径
     * @return 创建的道具句柄
     */
    public int loadFUItem(String bundle) {
        if(bundlesLoaded.containsKey(bundle)){
            return bundlesLoaded.get(bundle);
        }
        int item = 0;
        long loadItemS = System.currentTimeMillis();
        try {
            if (TextUtils.isEmpty(bundle)) {
                item = 0;
            } else {
                InputStream is  = mContext.getAssets().open(bundle);
                byte[] itemData = new byte[is.available()];
                is.read(itemData);
                is.close();
                item = faceunity.fuCreateItemFromPackage(itemData);
            }
            long loadItemE = System.currentTimeMillis();
            Log.i("time", "load item:" + bundle + "--loadTime:" + (loadItemE - loadItemS) + "ms");
            Log.i(TAG, "bundle loadFUItem " + bundle + " item " + item);
            bundlesLoaded.put(bundle, item);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }

    public FuVideoFrame processVideoFrame(byte[] img, int texId, float[] texMatrix, int width, int height, int cameraType) {
        if(isFuItemLoading || !isInitialized){
            return new FuVideoFrame(texId, texMatrix, width, height, TEXTURE_TYPE_OES);
        }
        if(mRotatedImage == null){
            mRotatedImage = new faceunity.RotatedImage();
        }
        int rotateMode = cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT ? faceunity.FU_ROTATION_MODE_270 : faceunity.FU_ROTATION_MODE_90;
        int flipX = cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT ? 1 : 0;
        int flipY = 0;
        faceunity.fuRotateImage(mRotatedImage, img, faceunity.FU_FORMAT_NV21_BUFFER, width, height, rotateMode, flipX, flipY);
        //设置texture的绘制方式
        faceunity.fuSetInputCameraMatrix(flipX, flipY, rotateMode);
        int rendWidth = mRotatedImage.mWidth;
        int rendHeight = mRotatedImage.mHeight;
        faceunity.fuSetOutputResolution(rendWidth, rendHeight);

        while (!mEventQueue.isEmpty()) {
            Runnable r = mEventQueue.remove(0);
            if (r != null)
                r.run();
        }

        int retTexId = faceunity.fuRenderBundlesWithCamera(mRotatedImage.mData,
                texId,
                faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE,
                rendWidth, rendHeight, mFrameId++, mItemsArray);

        //获取faceunity错误信息，并调用回调接口
        int error = faceunity.fuGetSystemError();
        if (error != 0 && !TextUtils.isEmpty(faceunity.fuGetSystemErrorString(error))) {
            Log.e(TAG, "fuGetSystemErrorString " + faceunity.fuGetSystemErrorString(error));
        }

        if(mFrameId < 3){
            return null;
        }

        return new FuVideoFrame(retTexId, IdentityMatrix, rendWidth, rendHeight, TEXTURE_TYPE_2D);
    }

    private void runInFuItemThread(Runnable runnable){
        if(fuItemThread == null || !fuItemThread.isAlive() || runnable == null){
            return;
        }
        ThreadUtils.invokeAtFrontUninterruptibly(fuItemHandler, runnable);
    }

    private void runInEventQueue(Runnable runnable){
        mEventQueue.add(runnable);
    }

    public static final class FuVideoFrame {
        public final int texId;
        public final float[] texMatrix;
        public final int width;
        public final int height;
        public final int texType;

        public FuVideoFrame(int texId, float[] texMatrix, int width, int height, int texType) {
            this.texId = texId;
            this.texMatrix = texMatrix;
            this.width = width;
            this.height = height;
            this.texType = texType;
        }
    }

    public static final int TEXTURE_TYPE_OES = 1;
    public static final int TEXTURE_TYPE_2D = 2;

}
