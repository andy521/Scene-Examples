package io.agora.scene.virtualimage.manager;

import android.content.Context;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.core.view.GestureDetectorCompat;

import com.faceunity.pta_art.constant.ColorConstant;
import com.faceunity.pta_art.constant.FilePathFactory;
import com.faceunity.pta_art.core.AvatarHandle;
import com.faceunity.pta_art.core.FUPTARenderer;
import com.faceunity.pta_art.core.PTACore;
import com.faceunity.pta_art.core.client.PTAClientWrapper;
import com.faceunity.pta_art.core.driver.ar.AvatarARDriveHandle;
import com.faceunity.pta_art.core.driver.ar.PTAARDriveCore;
import com.faceunity.pta_art.entity.AvatarPTA;
import com.faceunity.pta_art.entity.DBHelper;
import com.faceunity.pta_art.fragment.BaseFragment;
import com.faceunity.wrapper.faceunity;

import java.util.List;

public class FUManager {
    private static final String TAG = "FUDemoManager";
    private static final float[] IdentityMatrix = new float[16];
    static {
        Matrix.setIdentityM(IdentityMatrix, 0);
    }

    private static volatile FUManager INSTANCE;
    private PTAARDriveCore mARDriveCore;
    private AvatarARDriveHandle mARAvatarHandle;

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
    private volatile boolean isStarted = false;
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

    private boolean isARMode = false;


    public void initialize(Context context){
        if(isInitialized){
            return;
        }
        mContext = context.getApplicationContext();

        //初始化 core data 数据---捏脸
        PTAClientWrapper.setupData(mContext);
        PTAClientWrapper.setupStyleData(mContext);

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

        isStarted = true;
        isARMode = false;
        resetMode();
    }

    public void switchMode(){
        if(isARMode){
            switch2AvatarMode();
        }else{
            switch2ARMode();
        }
    }

    public boolean isARMode() {
        return isARMode;
    }

    public void switch2AvatarMode(){
        if(!isStarted){
            return;
        }
        isARMode = false;
        if(mARAvatarHandle != null){
            mARDriveCore.release();
            mARDriveCore = null;
            mARAvatarHandle = null;
            mFUP2ARenderer.setFUCore(mP2ACore);
        }
        mP2ACore.loadWholeBodyCamera();

        mAvatarHandle.setAvatar(getShowAvatarP2A(), true, true);
        mAvatarHandle.setNeedTrackFace(true);
        mAvatarHandle.openLight(FilePathFactory.BUNDLE_light);
        mAvatarHandle.setScale(new double[]{0.0, -50f, 300f});
        mAvatarHandle.setBackgroundColor("#AE8EF0");
    }

    private void resetMode() {
        if(isARMode){
            switch2ARMode();
        }
        else {
            switch2AvatarMode();
        }
    }

    public void switch2ARMode() {
        if(!isStarted){
            return;
        }
        isARMode = true;
        if(mARAvatarHandle == null){
            mP2ACore.unBind();
            mARDriveCore = new PTAARDriveCore(mContext, mFUP2ARenderer);
            mFUP2ARenderer.setFUCore(mARDriveCore);
            mARAvatarHandle = mARDriveCore.createAvatarARHandle();
        }
        mARAvatarHandle.setARAvatar(getShowAvatarP2A(), true);
        mAvatarHandle.disableBackgroundColor();
    }

    public void stop(){
        isStarted = false;
        if(mARAvatarHandle != null){
            mARDriveCore.release();
            mARDriveCore = null;
            mARAvatarHandle = null;
        }

        if(mFUP2ARenderer != null){
            mFUP2ARenderer.release();
        }

        mFUP2ARenderer = null;
        mAvatarHandle = null;
        mP2ACore = null;
        mRotatedImage = null;
    }

    public AvatarPTA getShowAvatarP2A() {
        return mShowAvatarP2A;
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

    private final BaseFragment.IContextCallback contextCallbackImpl = new BaseFragment.IContextCallback() {

        @Override
        public AvatarPTA getShowAvatarP2A() {
            return mShowAvatarP2A;
        }


        @Override
        public void setShowAvatarP2A(AvatarPTA avatarP2A) {
            mShowAvatarP2A = avatarP2A;
        }

        @Override
        public void updateAvatarP2As() {
            resetMode();
        }

    };

}
