package io.agora.scene.virtualimage.manager;


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
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.TextureView;
import android.widget.FrameLayout;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.base.internal.video.RendererCommon;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.DataStreamConfig;
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
            VD_1280x720,
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
    private final SparseArray<OnStreamMessageListener> dataStreamListener = new SparseArray<>();

    private static volatile RtcManager INSTANCE;
    public static RtcManager getInstance(){
        if(INSTANCE == null){
            synchronized (RtcManager.class){
                if(INSTANCE== null){
                    INSTANCE = new RtcManager();
                }
            }
        }
        return INSTANCE;
    }

    private RtcManager(){}

    private volatile boolean isPushExtVideoFrame = false;
    private IVideoFrameObserver videoFrameObserver = new IVideoFrameObserver() {
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

                final VideoPreProcess _videoPreProcess = RtcManager.this.videoPreProcess;
                final AgoraGLSurfaceView _localGLSurfaceView = RtcManager.this.localGLSurfaceView;
                if(_videoPreProcess != null){
                    if(videoPreTexBuffHelper == null){
                        videoPreTexBuffHelper = TextureBufferHelper.create("VideoPreProcess", textureBuffer.getEglBaseContext());
                        videoPreTexBuffHelper.invoke(new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                Log.d(TAG, "VideoPreProcess --> onTextureBufferHelperCreated thread=" + Thread.currentThread().getName());
                                _videoPreProcess.onTextureBufferHelperCreated(videoPreTexBuffHelper);
                                return null;
                            }
                        });
                    }
                    videoPreTexBuffHelper.invoke(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            ProcessVideoFrame processVideoFrame = _videoPreProcess.processVideoFrameTex(convertToNV21(videoFrame), texId, texMatrix, width, height,
                                    cameraDirection == CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT ? Camera.CameraInfo.CAMERA_FACING_FRONT: Camera.CameraInfo.CAMERA_FACING_BACK);
                            if(processVideoFrame == null){
                                return false;
                            }

                            if(_localGLSurfaceView != null){
                                _localGLSurfaceView.init(videoPreTexBuffHelper.getEglBase().getEglBaseContext());
                                if(processVideoFrame.texType == TEXTURE_TYPE_2D){
                                    _localGLSurfaceView.consume2DTexture(processVideoFrame.texId,
                                            processVideoFrame.texMatrix,
                                            processVideoFrame.width,
                                            processVideoFrame.height
                                    );
                                }
                                else{
                                    _localGLSurfaceView.consumeOESTexture(processVideoFrame.texId,
                                            processVideoFrame.texMatrix,
                                            processVideoFrame.width,
                                            processVideoFrame.height
                                    );
                                }
                            }

                            if(isPushExtVideoFrame){
                                VideoFrame.TextureBuffer pushTextBuffer = videoPreTexBuffHelper.wrapTextureBuffer(processVideoFrame.width, processVideoFrame.height,
                                        processVideoFrame.texType == TEXTURE_TYPE_2D ? VideoFrame.TextureBuffer.Type.RGB : VideoFrame.TextureBuffer.Type.OES,
                                        processVideoFrame.texId, RendererCommon.convertMatrixToAndroidGraphicsMatrix(processVideoFrame.texMatrix));
                                engine.pushExternalVideoFrame(new VideoFrame(pushTextBuffer, 0, System.nanoTime()));
                            }

                            return null;
                        }
                    });
                    return false;
                }else if(_localGLSurfaceView != null){
                    _localGLSurfaceView.init(textureBuffer.getEglBaseContext());
                    _localGLSurfaceView.consumeOESTexture(texId,
                            texMatrix,
                            width,
                            height
                    );
                }
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
    };

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
                    OnStreamMessageListener listener = dataStreamListener.get(streamId);
                    if(listener != null){
                        listener.onMessageReceived(streamId, uid, new String(data));
                    }
                }

                @Override
                public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached) {
                    super.onStreamMessageError(uid, streamId, error, missed, cached);
                    Log.d(TAG, "onStreamMessageError uid=" + uid + ",streamId=" + streamId + ",error=" + error + ",missed=" + missed + ",cached=" + cached);
                }

            });
            engine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            engine.setLogLevel(Constants.LogLevel.getValue(Constants.LogLevel.LOG_LEVEL_ERROR));

            engine.setAudioProfile(Constants.AUDIO_PROFILE_SPEECH_STANDARD, Constants.AUDIO_SCENARIO_GAME_STREAMING);
            engine.setDefaultAudioRoutetoSpeakerphone(true);
            engine.enableDualStreamMode(false);
            engine.registerVideoFrameObserver(videoFrameObserver);

            engine.enableVideo();
            engine.enableAudio();

            engine.setCameraCapturerConfiguration(new CameraCapturerConfiguration(cameraDirection, new CameraCapturerConfiguration.CaptureFormat(encoderConfiguration.dimensions.width, encoderConfiguration.dimensions.height, encoderConfiguration.frameRate)));
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

    public int createDataStream(OnStreamMessageListener listener){
        if(engine == null){
            return 0;
        }
        int dataStream = engine.createDataStream(new DataStreamConfig());
        dataStreamListener.put(dataStream, listener);
        return dataStream;
    }

    public void sendDataStreamMsg(int streamId, String msg){
        if(engine == null){
            return;
        }
        engine.sendStreamMessage(streamId, msg.getBytes(StandardCharsets.UTF_8));
    }

    public void renderLocalVideo(FrameLayout container, Runnable firstFrame) {
        if (engine == null) {
            return;
        }
        if(localGLSurfaceView != null){
            localGLSurfaceView.release();
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
        options.autoSubscribeAudio = true;
        options.autoSubscribeVideo = true;

        if(publish && videoPreProcess != null){
            /**Configures the external video source.
             * @param enable Sets whether or not to use the external video source:
             *                 true: Use the external video source.
             *                 false: Do not use the external video source.
             * @param useTexture Sets whether or not to use texture as an input:
             *                     true: Use texture as an input.
             *                     false: (Default) Do not use texture as an input.
             * @param pushMode Sets whether or not the external video source needs to call the PushExternalVideoFrame
             *                 method to send the video frame to the Agora SDK:
             *                   true: Use the push mode.
             *                   false: Use the pull mode (not supported).*/
            engine.setExternalVideoSource(true, true, false);
            isPushExtVideoFrame = true;
        }else{
            options.publishAudioTrack = publish;
            options.publishAudioTrack = publish;
        }

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
        TextureView view = new TextureView(container.getContext());
        container.addView(view);
        engine.setupRemoteVideo(new VideoCanvas(view, RENDER_MODE_HIDDEN, uid));
    }

    public void setVideoPreProcess(VideoPreProcess videoPreProcess) {
        this.videoPreProcess = videoPreProcess;
    }

    public void reset(boolean isStopPreview) {
        isPushExtVideoFrame = false;
        publishChannelListener = null;
        if(localGLSurfaceView != null){
            localGLSurfaceView.release();
            localGLSurfaceView = null;
        }
        VideoPreProcess _videoPreProcess = this.videoPreProcess;
        videoPreProcess = null;
        if(isStopPreview){
            if(videoPreTexBuffHelper != null){
                if(_videoPreProcess != null){
                    videoPreTexBuffHelper.invoke(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            Log.d(TAG, "c --> onTextureBufferHelperDestroy thread=" + Thread.currentThread().getName() + ",_videoPreProcess=" + _videoPreProcess);
                            _videoPreProcess.onTextureBufferHelperDestroy();
                            return null;
                        }
                    });
                }
                videoPreTexBuffHelper.dispose();
                videoPreTexBuffHelper = null;
            }
        }

        Log.d(TAG, "stopPreview --> reset isStopPreview=" + isStopPreview);

        if (engine != null) {
            engine.leaveChannel();
            if(isStopPreview){
                engine.stopPreview();
                engine.setCameraCapturerConfiguration(new CameraCapturerConfiguration(cameraDirection, new CameraCapturerConfiguration.CaptureFormat(encoderConfiguration.dimensions.width, encoderConfiguration.dimensions.height, encoderConfiguration.frameRate)));
            }
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

    public void muteLocalAudio(boolean mute){
        if(engine == null){
            return;
        }
        engine.muteLocalAudioStream(mute);
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

    public interface OnStreamMessageListener {
        void onMessageReceived(int dataStreamId, int fromUid, String message);
    }

    public interface VideoPreProcess {
        void onTextureBufferHelperCreated(TextureBufferHelper helper);
        ProcessVideoFrame processVideoFrameTex(byte[] img, int texId, float[] texMatrix, int width, int height, int cameraType);
        void onTextureBufferHelperDestroy();
    }

    public static final class ProcessVideoFrame{
        public final int texId;
        public final float[] texMatrix;
        public final int width;
        public final int height;
        public final int texType;

        public ProcessVideoFrame(int texId, float[] texMatrix, int width, int height, int texType) {
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
