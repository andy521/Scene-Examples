package io.agora.scene.virtualimage.manager;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

import androidx.core.view.GestureDetectorCompat;

import com.faceunity.pta_art.constant.ColorConstant;
import com.faceunity.pta_art.constant.FilePathFactory;
import com.faceunity.pta_art.core.AvatarHandle;
import com.faceunity.pta_art.core.FUPTARenderer;
import com.faceunity.pta_art.core.PTACore;
import com.faceunity.pta_art.core.authpack;
import com.faceunity.pta_art.core.client.PTAClientWrapper;
import com.faceunity.pta_art.entity.AvatarPTA;
import com.faceunity.pta_art.entity.DBHelper;
import com.faceunity.pta_art.fragment.BaseFragment;
import com.faceunity.pta_art.renderer.CameraRenderer;
import com.faceunity.pta_art.utils.sta.TtsEngineUtils;
import com.faceunity.pta_art.web.OkHttpUtils;
import com.faceunity.pta_helper.FUAuthCheck;
import com.faceunity.wrapper.faceunity;

import java.util.List;

public class FUDemoManager {
    private static final String TAG = "FUDemoManager";
    private static final float[] IdentityMatrix = new float[16];
    static {
        Matrix.setIdentityM(IdentityMatrix, 0);
    }
    private static volatile FUDemoManager INSTANCE;
    public static FUDemoManager getInstance(){
        if(INSTANCE == null){
            synchronized (FUDemoManager.class){
                if(INSTANCE == null){
                    INSTANCE = new FUDemoManager();
                }
            }
        }
        return INSTANCE;
    }
    private FUDemoManager(){}

    private volatile boolean isInitialized = false;
    private volatile boolean isStarted = false;
    private volatile boolean isFUAvatarPrepared = false;
    private Context mContext;
    private FUPTARenderer mFUP2ARenderer;
    private PTACore mP2ACore;
    private AvatarHandle mAvatarHandle;

    private DBHelper mDBHelper;
    private List<AvatarPTA> mAvatarP2As;
    private int mShowIndex;
    private AvatarPTA mShowAvatarP2A;

    private faceunity.RotatedImage mRotatedImage = null;

    private GestureDetectorCompat mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private int touchMode;


    public void initialize(Context context){
        if(isInitialized){
            return;
        }
        mContext = context.getApplicationContext();
        /**
         * 初始化dsp设备
         * 如果已经调用过一次了，后面再重新初始化bundle，也不需要重新再调用了。
         */
        String path = mContext.getApplicationInfo().nativeLibraryDir;
        faceunity.fuHexagonInitWithPath(path);

        OkHttpUtils.initOkHttpUtils(OkHttpUtils.initOkHttpClient());

        //初始化部分
        long startTime = System.currentTimeMillis();
        //初始化nama
        FUPTARenderer.initFURenderer(mContext);
        long endInitNamaTime = System.currentTimeMillis();

        //初始化P2A Helper ----工具类
        FUAuthCheck.fuP2ASetAuth(authpack.A());
        long endInitP2ATime = System.currentTimeMillis();

        //初始化 core data 数据---捏脸
            PTAClientWrapper.setupData(mContext);
            PTAClientWrapper.setupStyleData(mContext);
        long endInitCoreDataTime = System.currentTimeMillis();

        Log.i(TAG, "InitAllTime: " + (endInitCoreDataTime - startTime)
                + "\nInitNamaTime: " + (endInitNamaTime - startTime)
                + "\nInitP2ATime: " + (endInitP2ATime - endInitNamaTime)
                + "\nInitCoreDataTime: " + (endInitCoreDataTime - endInitP2ATime));

        TtsEngineUtils.getInstance().init(mContext);

        //风格选择后初始化 P2A client
        ColorConstant.init(mContext);

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        final int screenWidth = metrics.widthPixels;
        final int screenHeight = metrics.heightPixels;
        mGestureDetector = new GestureDetectorCompat(mContext, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if(mP2ACore != null){
                    mP2ACore.setNextHomeAnimationPosition();
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (touchMode != 1) {
                    touchMode = 1;
                    return false;
                }
                float rotDelta = -distanceX / screenWidth;
                if(mAvatarHandle != null){
                    mAvatarHandle.setRotDelta(rotDelta);
                }
                return distanceX != 0;
            }
        });
        mScaleGestureDetector = new ScaleGestureDetector(mContext, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scale = detector.getScaleFactor() - 1;
                if(mAvatarHandle != null){
                    mAvatarHandle.setScaleDelta(scale);
                }
                return scale != 0;
            }
        });

        mDBHelper = DBHelper.create(mContext);
        mAvatarP2As = mDBHelper.getAllAvatarP2As();
        Log.d(TAG, "mAvatarP2As=" + mAvatarP2As.toString());
        mShowIndex = mAvatarP2As.size() - 1;
        mShowAvatarP2A = mAvatarP2As.get(mShowIndex);

        isInitialized = true;
    }

    public void start() {
        if (isStarted) {
            return;
        }
        mFUP2ARenderer = new FUPTARenderer(mContext);
        mP2ACore = new PTACore(mContext, mFUP2ARenderer);
        mFUP2ARenderer.setFUCore(mP2ACore);

        mAvatarHandle = mP2ACore.createAvatarHandle();
        mAvatarHandle.setAvatar(getShowAvatarP2A(), new Runnable() {
            @Override
            public void run() {
                mAvatarHandle.openLight(FilePathFactory.BUNDLE_light);
                mAvatarHandle.setScale(new double[]{0.0, -50f, 300f});
                mAvatarHandle.setBackgroundColor("#AE8EF0");
                isFUAvatarPrepared = true;
            }
        });
        mP2ACore.loadWholeBodyCamera();

        isStarted = true;
    }

    public void stop(){
        isStarted = false;
        isFUAvatarPrepared = false;
        if(mFUP2ARenderer != null){
            mFUP2ARenderer.onSurfaceDestroyed();
        }

        mFUP2ARenderer = null;
        mAvatarHandle = null;
        mP2ACore = null;
        mRotatedImage = null;
    }

    public int getShowIndex() {
        return mShowIndex;
    }

    public AvatarPTA getShowAvatarP2A() {
        return mShowAvatarP2A;
    }

    public ResultFrame onDrawFrame(byte[] img, int texId, float[] texMatrix, int width, int height, int cameraType) {
        if (!isStarted) {
            return null;
        }
        int rotateMode = cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT ? faceunity.FU_ROTATION_MODE_270 : faceunity.FU_ROTATION_MODE_90;
        int flipX = cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT ? 1 : 0;
        int flipY = 0;

        if(mRotatedImage == null){
            mRotatedImage = new faceunity.RotatedImage();
        }

        faceunity.fuRotateImage(mRotatedImage, img, faceunity.FU_FORMAT_NV21_BUFFER, width, height, rotateMode, flipX, flipY);
        //设置texture的绘制方式
        faceunity.fuSetInputCameraMatrix(flipX, flipY, rotateMode);
        int rendWidth = mRotatedImage.mWidth;
        int rendHeight = mRotatedImage.mHeight;
        faceunity.fuSetOutputResolution(rendWidth, rendHeight);

        int retTexId = mFUP2ARenderer.onDrawFrame(mRotatedImage.mData, texId, rendWidth, rendHeight, 0);
        int error = faceunity.fuGetSystemError();
        if(error != 0){
            String errorInfo = faceunity.fuGetSystemErrorString(error);
            Log.e(TAG, "faceunity error code=" + error + ",info=" + errorInfo);
        }
        if(!isFUAvatarPrepared){
            return null;
        }

        return new ResultFrame(retTexId, IdentityMatrix, rendWidth, rendHeight, TEXTURE_TYPE_2D);
    }

    public List<AvatarPTA> getAvatarP2As() {
        return mAvatarP2As;
    }

    public FUPTARenderer getFUP2ARenderer() {
        return mFUP2ARenderer;
    }

    public PTACore getP2ACore() {
        return mP2ACore;
    }

    public AvatarHandle getAvatarHandle() {
        return mAvatarHandle;
    }

    public Context getContext() {
        return mContext;
    }

    public void handleTouchEvent(MotionEvent event){
        if(!isStarted || !isInitialized){
            return;
        }
        if (event.getPointerCount() == 2) {
            mScaleGestureDetector.onTouchEvent(event);
        } else if (event.getPointerCount() == 1) {
            mGestureDetector.onTouchEvent(event);
        }
    }

    public BaseFragment.IContextCallback getContextCallbackImple() {
        return contextCallbackImpl;
    }

    public static final class ResultFrame {
        public final int texId;
        public final float[] texMatrix;
        public final int width;
        public final int height;
        public final int texType;

        public ResultFrame(int texId, float[] texMatrix, int width, int height, int texType) {
            this.texId = texId;
            this.texMatrix = texMatrix;
            this.width = width;
            this.height = height;
            this.texType = texType;
        }
    }

    public static final int TEXTURE_TYPE_OES = 1;
    public static final int TEXTURE_TYPE_2D = 2;

    private final BaseFragment.IContextCallback contextCallbackImpl = new BaseFragment.IContextCallback() {

        @Override
        public void showHomeFragment() {
            // do nothing
        }

        @Override
        public AvatarPTA getShowAvatarP2A() {
            return mShowAvatarP2A;
        }

        @Override
        public int getShowIndex() {
            return mShowIndex;
        }

        @Override
        public void showBaseFragment(String tag) {
            // do nothing
        }

        @Override
        public PTACore getP2ACore() {
            return mP2ACore;
        }

        @Override
        public AvatarHandle getAvatarHandle() {
            return mAvatarHandle;
        }

        @Override
        public void setCanClick(boolean b, boolean b1) {

        }

        @Override
        public void setShowIndex(int position) {
            mShowIndex = position;
        }

        @Override
        public void setCanController(boolean b) {

        }

        @Override
        public void setShowAvatarP2A(AvatarPTA avatarP2A) {
            mShowAvatarP2A = avatarP2A;
        }

        @Override
        public void updateAvatarP2As() {
            mP2ACore.loadWholeBodyCamera();
            mP2ACore.setNeedTrackFace(true);
            mAvatarHandle.setScale(new double[]{0.0, -50f, 300f});
        }

        @Override
        public void setGLSurfaceViewSize(boolean b) {
            // do nothing
        }

        @Override
        public GLSurfaceView getmGLSurfaceView() {
            return null;
        }

        @Override
        public void initDebug(TextView mVersionText) {

        }

        @Override
        public FUPTARenderer getFUP2ARenderer() {
            return mFUP2ARenderer;
        }

        @Override
        public CameraRenderer getCameraRenderer() {
            return null;
        }

        @Override
        public List<AvatarPTA> getAvatarP2As() {
            return mAvatarP2As;
        }

        @Override
        public AvatarPTA getCurrentDrivenAvatar() {
            return null;
        }

        @Override
        public int getDrivenAvatarShowIndex() {
            return 0;
        }

        @Override
        public void setIsAR(boolean isAR) {

        }

        @Override
        public void setCurrentDrivenAvatar(AvatarPTA avatarPTA) {

        }

        @Override
        public boolean getIsAR() {
            return false;
        }
    };

}
