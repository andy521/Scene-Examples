package io.agora.sample.virtualimage.manager;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.faceunity.wrapper.faceunity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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


    private Context mContext;
    private int[] mToolHandlers;
    private int mToolController;

    private volatile boolean isInitialized = false;
    private int mFrameId;

    private HandlerThread fuItemThread;
    private Handler fuItemHandler;

    public static final int ITEM_ARRAYS_BG = 0;
    public static final int ITEM_ARRAYS_CONTROLLER = 1;
    public static final int ITEM_ARRAYS_EFFECT = 2;
    public static final int ITEM_ARRAYS_FXAA = 3;
    public static final int ITEM_ARRAYS_COUNT = 4;
    private final int[] mItemsArray = new int[ITEM_ARRAYS_COUNT];
    private faceunity.RotatedImage mRotatedImage;

    public static final float[] IdentityMatrix = new float[16];
    static {
        Matrix.setIdentityM(IdentityMatrix, 0);
    }

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

                    // 提前加载算法数据模型，用于人脸检测
                    loadAiModel(mContext, BUNDLE_ai_face_processor, faceunity.FUAITYPE_FACEPROCESSOR);

                    mToolController = loadFUItem(BUNDLE_controller_new);
                    int controllerConfig = loadFUItem(BUNDLE_controller_config_new);
                    faceunity.fuBindItems(mToolController, new int[]{controllerConfig});

                    int wholeBodyHandler = loadFUItem("new/camera/cam_02.bundle");
                    faceunity.fuBindItems(mToolController, new int[]{wholeBodyHandler});

                    loadAiModel(mContext, "ai_human_processor.bundle", faceunity.FUAITYPE_HUMAN_PROCESSOR);
                    faceunity.fuItemSetParam(mToolController, "enable_human_processor", 1.0);
                    //设置enable_face_processor，说明启用或者关闭面部追踪，value = 1.0表示开启，value = 0.0表示关闭
                    faceunity.fuItemSetParam(mToolController, "enable_face_processor", 1.0);

                    // 背景
                    int defaultBgHandler = loadFUItem("default_bg.bundle");
                    int planeLeftHandler = loadFUItem("plane_shadow_left.bundle");
                    int planeRightHandler = loadFUItem("plane_shadow_right.bundle");
                    faceunity.fuBindItems(mToolController, new int[]{defaultBgHandler});
                    faceunity.fuBindItems(mToolController, new int[]{planeLeftHandler, planeRightHandler});

                    // 加载人物道具
                    int headHandler = loadFUItem("new/head/head_1/head.bundle");
                    int hairHandler = loadFUItem("new/hair/female_hair_7.bundle");
                    int hatHandler = loadFUItem("new/hat/hair_hat_7.bundle");
                    int glassHandler = loadFUItem("new/glasses/glass_1.bundle");
                    int beardHandler = loadFUItem("new/beard/beard_4.bundle");
                    int eyebrowHandler = loadFUItem("new/eyebrow/eyebrow_3.bundle");
                    int eyelashHandler = loadFUItem("new/eyelash/Eyelash_1.bundle");
                    int bodyHandle = loadFUItem("new/body/female/midBody_female2.bundle");
                    int clothHandler = loadFUItem("new/clothes/suit/cloth_1.bundle");
                    int clothUpperHandler = loadFUItem("new/clothes/upper/shangyi_chenshan_1.bundle");
                    int clothLowerHandler = loadFUItem("new/clothes/lower/kuzi_changku_1.bundle");
                    int shoeHandler = loadFUItem("new/shoes/female_cloth03_shoes.bundle");

                    int decorationsEarHandler = loadFUItem("new/decorations/ear/peishi_erding_4.bundle");
                    int decorationsFootHandler = loadFUItem("new/decorations/foot/peishi_jiao_1.bundle");
                    int decorationsHandHandler = loadFUItem("new/decorations/hand/peishi_shou_1.bundle");
                    int decorationsHeadHandler = loadFUItem("new/decorations/head/toushi_8.bundle");
                    int decorationsNeckHandler = loadFUItem("new/decorations/neck/peishi_erji.bundle");

                    int expressionHandler = loadFUItem("new/expression/ani_idle.bundle");

                    int eyelinerHandler = loadFUItem("new/makeup/eyeliner/Eyeliner_1.bundle");
                    int eyeshadowHandler = loadFUItem("new/makeup/eyeshadow/Eyeshadow_1.bundle");
                    int facemakeupHandler = loadFUItem("new/makeup/facemakeup/facemakeup_2.bundle");
                    int lipglossHandler = loadFUItem("new/makeup/lipgloss/lipgloss_1.bundle");
                    int pupilHandler = loadFUItem("new/makeup/pupil/pupil_2.bundle");

                    mToolHandlers = new int[]{headHandler, hairHandler, hatHandler, glassHandler,
                            beardHandler, eyebrowHandler, eyelashHandler,
                            bodyHandle, clothHandler, clothUpperHandler, clothLowerHandler, shoeHandler,
                            decorationsEarHandler, decorationsFootHandler, decorationsHandHandler, decorationsHeadHandler,decorationsNeckHandler,
                            expressionHandler,
                            eyelinerHandler, eyeshadowHandler, facemakeupHandler, lipglossHandler, pupilHandler
                    };
                    faceunity.fuBindItems(mToolController, mToolHandlers);

                    faceunity.fuItemSetParam(mToolController, "human_3d_track_set_scene", 1);

                    faceunity.fuItemSetParam(mToolController, "target_position", new double[]{0.0, -30f, -100f});
                    faceunity.fuItemSetParam(mToolController, "target_angle", 0);
                    faceunity.fuItemSetParam(mToolController, "reset_all", 3);

                    mItemsArray[ITEM_ARRAYS_CONTROLLER] = mToolController;
                    mItemsArray[ITEM_ARRAYS_FXAA] = loadFUItem(BUNDLE_fxaa);

                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }

    public FuVideoFrame processVideoFrame(byte[] img, int texId, int width, int height, int cameraType) {
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

        int retTexId = faceunity.fuRenderBundlesWithCamera(mRotatedImage.mData,
                texId,
                faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE,
                rendWidth, rendHeight, mFrameId++, mItemsArray);

        return new FuVideoFrame(retTexId, IdentityMatrix, rendWidth, rendHeight, TEXTURE_TYPE_2D);
    }

    private void runInFuItemThread(Runnable runnable){
        if(fuItemThread == null || !fuItemThread.isAlive() || runnable == null){
            return;
        }
        fuItemHandler.post(runnable);
    }

    public void release() {
        mFrameId = 0;
        mRotatedImage = null;
        if(isInitialized){
            fuItemThread.quit();
            fuItemHandler.removeCallbacksAndMessages(null);
            isInitialized = false;
        }
        faceunity.fuDestroyAllItems();
        faceunity.fuOnDeviceLost();
        faceunity.fuDone();

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
