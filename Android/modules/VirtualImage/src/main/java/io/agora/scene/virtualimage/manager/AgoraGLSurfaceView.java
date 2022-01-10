package io.agora.scene.virtualimage.manager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import io.agora.base.internal.ThreadUtils;
import io.agora.base.internal.video.EglBase;
import io.agora.base.internal.video.EglBase10;
import io.agora.base.internal.video.EglBase14;
import io.agora.base.internal.video.GlRectDrawer;

public class AgoraGLSurfaceView extends SurfaceView {
    private Runnable pendingRunOnSurfaceCreated = null;
    private final GlRectDrawer mGlRectDrawer = new GlRectDrawer();
    private EglBase mEglBase;
    private Handler mHandler;
    private int mSurfaceWidth, mSurfaceHeight;

    public AgoraGLSurfaceView(Context context) {
        this(context, null);
    }

    public AgoraGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AgoraGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        SurfaceHolder holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if(pendingRunOnSurfaceCreated != null){
                    pendingRunOnSurfaceCreated.run();
                    pendingRunOnSurfaceCreated = null;
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                mSurfaceWidth = width;
                mSurfaceHeight = height;
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                release();
            }
        });
    }

    public void init(EglBase.Context sharedContext){
        if(mEglBase != null || pendingRunOnSurfaceCreated != null){
            return;
        }
        HandlerThread thread = new HandlerThread("AgoraGLSurfaceRender");
        thread.start();
        final Handler handler = new Handler(thread.getLooper());
        runOnSurfaceCreated(()-> initGLEnv(sharedContext, handler));
    }

    private void initGLEnv(EglBase.Context sharedContext, Handler handler) {
        final Surface surface = getHolder().getSurface();
        if(surface == null || !surface.isValid()){
            return;
        }
        ThreadUtils.invokeAtFrontUninterruptibly(handler, () -> {
            try {
                mEglBase = null;
                if(!EglBase14.isEGL14Supported() || sharedContext != null && !(sharedContext instanceof EglBase14.Context)){
                    mEglBase = new EglBase10((EglBase10.Context)sharedContext, EglBase.CONFIG_PIXEL_BUFFER);
                }else{
                    mEglBase = new EglBase14((EglBase14.Context)sharedContext, EglBase.CONFIG_PIXEL_BUFFER);
                }
                mEglBase.createSurface(surface);
                mEglBase.makeCurrent();
                mHandler = handler;
            } catch (RuntimeException ex) {
                this.mEglBase.release();
                handler.getLooper().quit();
                throw ex;
            }
            return true;
        });
    }

    public void consumeOESTexture(int textureId, float[] texMatrix, int width, int height) {
        if (mHandler == null) {
            return;
        }
        ThreadUtils.invokeAtFrontUninterruptibly(mHandler, () -> {
            mGlRectDrawer.drawOes(textureId, texMatrix, width, height, 0, 0, mSurfaceWidth, mSurfaceHeight);
            mEglBase.swapBuffers();
        });
    }

    public void consume2DTexture(int textureId, float[] texMatrix, int width, int height) {
        if (mHandler == null) {
            return;
        }
        ThreadUtils.invokeAtFrontUninterruptibly(mHandler, () -> {
            mGlRectDrawer.drawRgb(textureId, texMatrix, width, height, 0, 0, mSurfaceWidth, mSurfaceHeight);
            mEglBase.swapBuffers();
        });
    }

    private void runOnSurfaceCreated(Runnable runnable){
        final Surface surface = getHolder().getSurface();
        if(surface == null){
            pendingRunOnSurfaceCreated = runnable;
        }else{
            runnable.run();
        }
    }

    public void release() {
        pendingRunOnSurfaceCreated = null;
        if(mHandler != null){
            ThreadUtils.invokeAtFrontUninterruptibly(mHandler, new Runnable() {
                public void run() {
                    if (mGlRectDrawer != null) {
                        mGlRectDrawer.release();
                    }

                    if(mEglBase != null){
                        mEglBase.release();
                        mEglBase = null;
                    }

                    if(mHandler != null){
                        mHandler.getLooper().quit();
                        mHandler = null;
                    }
                }
            });
        }
    }


}
