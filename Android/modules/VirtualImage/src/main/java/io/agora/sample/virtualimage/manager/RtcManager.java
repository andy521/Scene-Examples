package io.agora.sample.virtualimage.manager;


import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_1;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_10;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_7;
import static io.agora.rtc2.video.VideoEncoderConfiguration.MIRROR_MODE_TYPE;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_120x120;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_1280x720;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_160x120;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_180x180;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_240x180;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_240x240;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_320x180;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_320x240;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_360x360;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_424x240;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_480x360;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_480x480;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_640x360;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_640x480;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_840x480;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_960x720;

import android.content.Context;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.base.internal.video.EglBase;
import io.agora.base.internal.video.RendererCommon;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.CameraCapturerConfiguration;
import io.agora.rtc2.video.IVideoFrameObserver;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;


public class RtcManager {
    private static final String TAG = "RtcManager";
    private static final int LOCAL_RTC_UID = 0;
    public static final List<VideoEncoderConfiguration.VideoDimensions> sVideoDimensions = Arrays.asList(
            VD_120x120,
            VD_160x120,
            VD_180x180,
            VD_240x180,
            VD_320x180,
            VD_240x240,
            VD_320x240,
            VD_424x240,
            VD_360x360,
            VD_480x360,
            VD_640x360,
            VD_480x480,
            VD_640x480,
            VD_840x480,
            VD_960x720,
            VD_1280x720
    );
    public static final List<FRAME_RATE> sFrameRates = Arrays.asList(
            FRAME_RATE_FPS_1,
            FRAME_RATE_FPS_7,
            FRAME_RATE_FPS_10,
            FRAME_RATE_FPS_15,
            FRAME_RATE_FPS_24,
            FRAME_RATE_FPS_30
    );


    private static CameraCapturerConfiguration.CAMERA_DIRECTION cameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT;
    public static final VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
            VD_640x360,
            FRAME_RATE_FPS_15,
            700,
            ORIENTATION_MODE_FIXED_PORTRAIT
    );

    private volatile boolean isInitialized = false;
    private RtcEngine engine;
    private final Map<Integer, Runnable> firstVideoFramePendingRuns = new HashMap<>();
    private OnChannelListener publishChannelListener;
    private String publishChannelId;

    private final Matrix localRenderMatrix = new Matrix();
    private AgoraGLSurfaceView localGLSurfaceView;

    private VideoPreProcess videoPreProcess;
    private TextureBufferHelper videoPreTexBuffHelper;

    public void init(Context context, String appId, OnInitializeListener listener) {
        if (isInitialized) {
            return;
        }
        try {
            // 0. create engine
            engine = RtcEngine.create(context.getApplicationContext(), appId, new IRtcEngineEventHandler() {
                @Override
                public void onWarning(int warn) {
                    super.onWarning(warn);
                    Log.w(TAG, String.format("onWarning code %d message %s", warn, RtcEngine.getErrorDescription(warn)));
                }

                @Override
                public void onError(int err) {
                    super.onError(err);
                    Log.e(TAG, String.format("onError code %d", err));
                    if (err == ErrorCode.ERR_OK) {
                        if (listener != null) {
                            listener.onSuccess();
                        }
                    } else {
                        if (listener != null) {
                            listener.onError(err, err == ErrorCode.ERR_INVALID_TOKEN ? "invalid token" : "");
                        }
                        if (publishChannelListener != null) {
                            publishChannelListener.onError(err, "");
                        }
                    }
                }

                @Override
                public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
                    super.onFirstLocalVideoFrame(width, height, elapsed);

                    Log.d(TAG, "onFirstLocalVideoFrame");
                    Runnable runnable = firstVideoFramePendingRuns.get(LOCAL_RTC_UID);
                    if (runnable != null) {
                        runnable.run();
                        firstVideoFramePendingRuns.remove(LOCAL_RTC_UID);
                    }
                }

                @Override
                public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                    super.onJoinChannelSuccess(channel, uid, elapsed);
                    if (publishChannelId.equals(channel)) {
                        if (publishChannelListener != null) {
                            publishChannelListener.onJoinSuccess(uid);
                        }
                    }
                }

                @Override
                public void onUserJoined(int uid, int elapsed) {
                    super.onUserJoined(uid, elapsed);
                    if (publishChannelListener != null) {
                        publishChannelListener.onUserJoined(publishChannelId, uid);
                    }
                }

                @Override
                public void onUserOffline(int uid, int reason) {
                    super.onUserOffline(uid, reason);
                    if (publishChannelListener != null) {
                        publishChannelListener.onUserOffline(publishChannelId, uid);
                    }
                }

                @Override
                public void onRtcStats(RtcStats stats) {
                    super.onRtcStats(stats);
                }

                @Override
                public void onLastmileProbeResult(LastmileProbeResult result) {
                    super.onLastmileProbeResult(result);

                }

                @Override
                public void onRemoteVideoStats(RemoteVideoStats stats) {
                    super.onRemoteVideoStats(stats);
                }

                @Override
                public void onStreamMessage(int uid, int streamId, byte[] data) {
                    super.onStreamMessage(uid, streamId, data);
                    Log.d(TAG, "onStreamMessage uid=" + uid + ",streamId=" + streamId + ",data=" + new String(data));
                }

                @Override
                public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached) {
                    super.onStreamMessageError(uid, streamId, error, missed, cached);
                    Log.d(TAG, "onStreamMessageError uid=" + uid + ",streamId=" + streamId + ",error=" + error + ",missed=" + missed + ",cached=" + cached);
                }

            });
            engine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);

            engine.setAudioProfile(Constants.AUDIO_PROFILE_SPEECH_STANDARD, Constants.AUDIO_SCENARIO_GAME_STREAMING);
            engine.setDefaultAudioRoutetoSpeakerphone(true);
            engine.enableDualStreamMode(false);

            engine.registerVideoFrameObserver(new IVideoFrameObserver() {
                @Override
                public boolean onCaptureVideoFrame(VideoFrame videoFrame) {
                    VideoFrame.Buffer buffer = videoFrame.getBuffer();
                    if(buffer instanceof VideoFrame.TextureBuffer){
                        VideoFrame.TextureBuffer textureBuffer = (VideoFrame.TextureBuffer) buffer;

                        localRenderMatrix.reset();
                        localRenderMatrix.preTranslate(0.5f, 0.5f);
                        localRenderMatrix.preScale(1f, -1f); // I420-frames are upside down
                        localRenderMatrix.preRotate(videoFrame.getRotation());
                        localRenderMatrix.preTranslate(-0.5f, -0.5f);

                        float[] texMatrix = RendererCommon.convertMatrixFromAndroidGraphicsMatrix(localRenderMatrix);
                        int texId = textureBuffer.getTextureId();
                        int width = textureBuffer.getWidth();
                        int height = textureBuffer.getHeight();

                        if(videoPreProcess != null){
                            if(videoPreTexBuffHelper == null){
                                videoPreTexBuffHelper = TextureBufferHelper.create("VideoPreProcess", textureBuffer.getEglBaseContext());
                            }
                            videoPreTexBuffHelper.invoke(new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    int _texId = videoPreProcess.processVideoFrameTex(convertToNV21(videoFrame), texId, width, height);

                                    if(localGLSurfaceView != null){
                                        localGLSurfaceView.init(videoPreTexBuffHelper.getEglBase().getEglBaseContext());
                                        if(_texId != texId){
                                            localGLSurfaceView.consume2DTexture(_texId,
                                                    texMatrix,
                                                    width,
                                                    height
                                            );
                                        }
                                        else{
                                            localGLSurfaceView.consumeOESTexture(_texId,
                                                    texMatrix,
                                                    width,
                                                    height
                                            );
                                        }
                                    }

                                    return null;
                                }
                            });
                        }else if(localGLSurfaceView != null){
                            localGLSurfaceView.init(textureBuffer.getEglBaseContext());
                            localGLSurfaceView.consumeOESTexture(texId,
                                    texMatrix,
                                    width,
                                    height
                            );
                        }

                        return false;
                    }
                    return true;
                }

                @Override
                public boolean onScreenCaptureVideoFrame(VideoFrame videoFrame) {
                    return true;
                }

                @Override
                public boolean onMediaPlayerVideoFrame(VideoFrame videoFrame, int i) {
                    return true;
                }

                @Override
                public boolean onRenderVideoFrame(String s, int i, VideoFrame videoFrame) {
                    return true;
                }

                @Override
                public int getVideoFrameProcessMode() {
                    return PROCESS_MODE_READ_WRITE;
                }

                @Override
                public int getVideoFormatPreference() {
                    return 11;
                }

                @Override
                public int getRotationApplied() {
                    return 0;
                }

                @Override
                public boolean getMirrorApplied() {
                    return false;
                }
            });

            engine.enableVideo();
            engine.enableAudio();

            engine.setCameraCapturerConfiguration(new CameraCapturerConfiguration(cameraDirection));
            if(cameraDirection == CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT){
                encoderConfiguration.mirrorMode = MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED;
                engine.setVideoEncoderConfiguration(encoderConfiguration);
            }else{
                encoderConfiguration.mirrorMode = MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED;
                engine.setVideoEncoderConfiguration(encoderConfiguration);
            }

            isInitialized = true;
        } catch (Exception e) {
            if (listener != null) {
                listener.onError(-1, "RtcEngine create exception : " + e.toString());
            }
        }
    }

    private byte[] convertToNV21(VideoFrame videoFrame) {
        VideoFrame.Buffer buffer = videoFrame.getBuffer();

        VideoFrame.I420Buffer i420Buffer = buffer.toI420();
        int width = i420Buffer.getWidth();
        int height = i420Buffer.getHeight();

        ByteBuffer bufferY = i420Buffer.getDataY();
        ByteBuffer bufferU = i420Buffer.getDataU();
        ByteBuffer bufferV = i420Buffer.getDataV();

        byte[] i420 = YUVUtils.toWrappedI420(bufferY, bufferU, bufferV, width, height);
        byte[] nv21 = YUVUtils.I420ToNV21(i420, width, height);
        i420Buffer.release();
        return nv21;
    }

    public void renderLocalVideo(FrameLayout container, Runnable firstFrame) {
        if (engine == null) {
            return;
        }
        AgoraGLSurfaceView glSurfaceView = new AgoraGLSurfaceView(container.getContext());
        container.addView(glSurfaceView);
        localGLSurfaceView = glSurfaceView;
        engine.startPreview();
    }

    public void joinChannel(String channelId, String uid, String token, boolean publish, OnChannelListener listener) {
        if (engine == null) {
            return;
        }
        int _uid = LOCAL_RTC_UID;
        if(!TextUtils.isEmpty(uid)){
            try {
                _uid = Integer.parseInt(uid);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.publishCameraTrack = publish;
        options.publishAudioTrack = publish;

        engine.setClientRole(publish ? Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE);

        publishChannelId = channelId;
        publishChannelListener = new OnChannelListener() {
            @Override
            public void onError(int code, String message) {
                if (listener != null) {
                    listener.onError(code, message);
                }
            }

            @Override
            public void onJoinSuccess(int uid) {
                if (listener != null) {
                    listener.onJoinSuccess(uid);
                }
            }

            @Override
            public void onUserJoined(String channelId, int uid) {
                if (listener != null) {
                    listener.onUserJoined(channelId, uid);
                }
            }

            @Override
            public void onUserOffline(String channelId, int uid) {
                if (listener != null) {
                    listener.onUserOffline(channelId, uid);
                }
            }

        };

        int ret = engine.joinChannel(token, channelId, _uid, options);
        Log.i(TAG, String.format("joinChannel channel %s ret %d", channelId, ret));
    }

    public void renderRemoteVideo(FrameLayout container, int uid) {
        if (engine == null) {
            return;
        }
        SurfaceView view = new SurfaceView(container.getContext());
        container.addView(view);
        engine.setupRemoteVideo(new VideoCanvas(view, RENDER_MODE_HIDDEN, uid));
    }

    public void setVideoPreProcess(VideoPreProcess videoPreProcess) {
        this.videoPreProcess = videoPreProcess;
    }

    public void release() {
        publishChannelListener = null;
        if(localGLSurfaceView != null){
            localGLSurfaceView.release();
            localGLSurfaceView = null;
        }
        videoPreProcess = null;
        if(videoPreTexBuffHelper != null){
            videoPreTexBuffHelper.dispose();
            videoPreTexBuffHelper = null;
        }
        if (engine != null) {
            engine.leaveChannel();
            engine.stopPreview();
            RtcEngine.destroy();
        }
    }

    public void switchCamera() {
        if (engine == null) {
            return;
        }
        engine.switchCamera();
        if (cameraDirection == CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT) {
            cameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_REAR;
            encoderConfiguration.mirrorMode = MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED;
        } else {
            cameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT;
            encoderConfiguration.mirrorMode = MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED;
        }
        engine.setVideoEncoderConfiguration(encoderConfiguration);
    }


    public interface OnInitializeListener {
        void onError(int code, String message);

        void onSuccess();
    }

    public interface OnChannelListener {
        void onError(int code, String message);

        void onJoinSuccess(int uid);

        void onUserJoined(String channelId, int uid);

        void onUserOffline(String channelId, int uid);
    }

    public interface VideoPreProcess {
        int processVideoFrameTex(byte[] img, int texId, int width, int height);
    }
}
