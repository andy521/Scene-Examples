package io.agora.sample.virtualimage.manager;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.faceunity.wrapper.faceunity;

import java.io.File;
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


    private static volatile FUManager INSTANCE;
    private Context mContext;
    private int[] mToolHandlers;
    private int mToolController;

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

    private volatile boolean isInitialized = false;
    private int mFrameId;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private HandlerThread fuItemThread;
    private Handler fuItemHandler;

    public static final int ITEM_ARRAYS_BG = 0;
    public static final int ITEM_ARRAYS_CONTROLLER = 1;
    public static final int ITEM_ARRAYS_EFFECT = 2;
    public static final int ITEM_ARRAYS_FXAA = 3;
    public static final int ITEM_ARRAYS_COUNT = 4;
    private final int[] mItemsArray = new int[ITEM_ARRAYS_COUNT];

    public void init(Context ct){
        if(isInitialized){
            return;
        }
        mContext = ct.getApplicationContext();
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

            fuItemThread = new HandlerThread("FuItemLoad");
            fuItemThread.start();
            fuItemHandler = new Handler(fuItemThread.getLooper());

            runInFuItemThread(new Runnable() {
                @Override
                public void run() {
                    mToolController = loadFUItem(BUNDLE_controller_new);
                    int controllerConfig = loadFUItem(BUNDLE_controller_config_new);
                    faceunity.fuBindItems(mToolController, new int[]{controllerConfig});

                    /**
                     * 1 为开启，0 为关闭，开启的时候移动角色的值会被设进骨骼系统，这时候带DynamicBone的模型会有相关效果
                     * 如果添加了没有骨骼的模型，请关闭这个值，否则无法移动模型
                     * 默认开启
                     * 每个角色的这个值都是独立的
                     */
                    faceunity.fuItemSetParam(mToolController, "modelmat_to_bone", 0);
                    int hairMask = loadFUItem("hair_mask.bundle");
                    faceunity.fuItemSetParam(mToolController, "enter_ar_mode", 1);
                    faceunity.fuBindItems(mToolController, new int[]{hairMask});
                    //3.设置enable_face_processor，说明启用或者关闭面部追踪，value = 1.0表示开启，value = 0.0表示关闭
                    faceunity.fuItemSetParam(mToolController, "enable_face_processor", 1.0);

                    // 加载人物道具
                    // 1. 头
                    int headHandler = loadFUItem("new/head/head_1/head.bundle");
                    int hairHandler = loadFUItem("new/hair/female_hair_7.bundle");
                    int hatHandler = loadFUItem("new/hat/hair_hat_7.bundle");
                    int glassHandler = loadFUItem("new/glasses/glass_1.bundle");
                    int beardHandler = loadFUItem("new/beard/beard_4.bundle");
                    int eyebrowHandler = loadFUItem("new/eyebrow/eyebrow_3.bundle");
                    int eyelashHandler = loadFUItem("new/eyelash/Eyelash_1.bundle");
                    int decorationsEarHandler = loadFUItem("new/decorations/ear/peishi_erding_4.bundle");
                    int decorationsHeadHandler = loadFUItem("new/decorations/head/toushi_8.bundle");
                    int eyelinerHandler = loadFUItem("new/makeup/eyeliner/Eyeliner_1.bundle");
                    int eyeshadowHandler = loadFUItem("new/makeup/eyeshadow/Eyeshadow_1.bundle");
                    int facemakeupHandler = loadFUItem("new/makeup/facemakeup/facemakeup_2.bundle");
                    int lipglossHandler = loadFUItem("new/makeup/lipgloss/lipgloss_1.bundle");
                    int pupilHandler = loadFUItem("new/makeup/pupil/pupil_2.bundle");

                    mToolHandlers = new int[]{headHandler, hairHandler, hatHandler, glassHandler,
                            beardHandler, eyebrowHandler, eyelashHandler,
                            decorationsEarHandler, decorationsHeadHandler,
                            eyelinerHandler, eyeshadowHandler,
                            facemakeupHandler, lipglossHandler,
                            pupilHandler
                    };
                    faceunity.fuBindItems(mToolController, mToolHandlers);

                    faceunity.fuItemSetParam(mToolController, "screen_orientation", 1);
                    faceunity.fuSetDefaultRotationMode(1);
                    faceunity.fuItemSetParam(mToolController, "is3DFlipH", 0);
                    faceunity.fuItemSetParam(mToolController, "arMode", (360 - 90) / 90);


                    mItemsArray[ITEM_ARRAYS_CONTROLLER] = mToolController;
                    mItemsArray[ITEM_ARRAYS_FXAA] = loadFUItem(BUNDLE_fxaa);
                }
            });

            isInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public int processVideoFrame(byte[] img, int texId, int width, int height) {
        return faceunity.fuRenderBundlesWithCamera(img,
                texId,
                faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE,
                width, height, mFrameId++, mItemsArray);
    }

    private void runInFuItemThread(Runnable runnable){
        if(fuItemThread == null || !fuItemThread.isAlive() || runnable == null){
            return;
        }
        fuItemHandler.post(runnable);
    }

}
