package io.agora.livepk.manager;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.agora.mediaplayer.IMediaPlayer;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.ClientRoleOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.LeaveChannelOptions;
import io.agora.rtc2.RtcConnection;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.live.LiveTranscoding;
import io.agora.rtc2.video.CameraCapturerConfiguration;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_640x360;

public class RtcManager {
    private static final String TAG = "RtcManager";
    private int publishUid = 0;
    private String publishChannelId = "";

    private volatile boolean isInitialized = false;
    private RtcEngineEx engine;
    private final Map<String, RtcConnection> mRtcChannels = new HashMap<>();
    private final Map<Integer, Runnable> firstVideoFramePendingRuns = new HashMap<>();
    private Context mContext;
    private OnChannelListener publishChannelListener = null;

    private static CameraCapturerConfiguration.CAMERA_DIRECTION sCameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT;

    private final VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
            VD_640x360,
            FRAME_RATE_FPS_15,
            600,
            ORIENTATION_MODE_FIXED_PORTRAIT
    );

    public void init(Context context, String appId, OnInitializeListener listener){
        if(isInitialized){
            return;
        }
        mContext = context;
        try {
            // 0. create engine
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = context;
            config.mAppId = appId;
            config.mChannelProfile =Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
            config.mEventHandler = new IRtcEngineEventHandler() {
                @Override
                public void onWarning(int warn) {
                    super.onWarning(warn);
                    Log.w(TAG, String.format("onWarning code %d message %s", warn, RtcEngine.getErrorDescription(warn)));
                }

                @Override
                public void onError(int err) {
                    super.onError(err);
                    Log.e(TAG, String.format("onError code %d", err));
                    if(err == ErrorCode.ERR_OK){
                        if (listener != null) {
                            listener.onSuccess();
                        }
                    }else{
                        if (listener != null) {
                            listener.onError(err, err == ErrorCode.ERR_INVALID_TOKEN ? "invalid token" : "");
                        }
                    }
                }

                @Override
                public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
                    super.onFirstLocalVideoFrame(width, height, elapsed);
                    Log.d(TAG, "onFirstLocalVideoFrame");
                }

                @Override
                public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                    super.onJoinChannelSuccess(channel, uid, elapsed);
                    Log.d(TAG, "onJoinChannelSuccess channel=" + channel + ",uid=" + uid);
                    if(channel.equals(publishChannelId)){
                        publishUid = uid;
                        if(publishChannelListener != null){
                            publishChannelListener.onJoinSuccess(uid);
                        }
                    }
                }

                @Override
                public void onUserJoined(int uid, int elapsed) {
                    super.onUserJoined(uid, elapsed);
                    Log.d(TAG, "onUserJoined uid=" + uid);
                    if(publishChannelListener != null){
                        publishChannelListener.onUserJoined(publishChannelId, uid);
                    }
                }

                @Override
                public void onUserOffline(int uid, int reason) {
                    super.onUserOffline(uid, reason);
                    Log.d(TAG, "onUserOffline uid=" + uid);
                }

            };
            config.mAudioScenario = Constants.AudioScenario.getValue(Constants.AudioScenario.GAME_STREAMING);
            engine = (RtcEngineEx)RtcEngineEx.create(config);
            engine.setDefaultAudioRoutetoSpeakerphone(true);
            engine.enableVideo();
            engine.enableAudio();

            engine.setParameters("{\"rtc.dual_signaling_mode\":2}");
            engine.setParameters("{\"rtc.work_manager_account_list\":[\"WM-raw_streaming-119.188.27.100\"]}");
            engine.setParameters("{\"rtc.work_manager_addr_list\":[\"119.188.27.100:30555\"]}");

            engine.setCameraCapturerConfiguration(new CameraCapturerConfiguration(sCameraDirection));
            if(sCameraDirection == CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT){
                encoderConfiguration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED;
            }else{
                encoderConfiguration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED;
            }
            engine.setVideoEncoderConfiguration(encoderConfiguration);

            isInitialized = true;
        } catch (Exception e) {
            if(listener != null){
                listener.onError(-1, "RtcEngine create exception : " + e.toString());
            }
        }
    }

    public void renderLocalVideo(FrameLayout container, Runnable firstFrame) {
        if (engine == null) {
            return;
        }
        View videoView = new SurfaceView(container.getContext());
        container.addView(videoView);
        if(firstFrame != null){
            firstFrame.run();
        }
        engine.setupLocalVideo(new VideoCanvas(videoView, RENDER_MODE_HIDDEN, publishUid));
        engine.startPreview();
    }

    public void joinChannel(String channelId, boolean publish, OnChannelListener listener){
        if (engine == null) {
            return;
        }
        final String pushRtmpUrl = getPushRtmpUrl(channelId);
        if(publish){
            ClientRoleOptions clientRoleOptions = new ClientRoleOptions();
            clientRoleOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY;
            engine.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_BROADCASTER, clientRoleOptions);
            engine.setVideoEncoderConfiguration(encoderConfiguration);
            publishChannelListener = new OnChannelListener() {
                @Override
                public void onError(int code, String message) {
                    if(listener != null){
                        listener.onError(code, message);
                    }
                }

                @Override
                public void onJoinSuccess(int uid) {
                    //LiveTranscoding transcoding = new LiveTranscoding();
                    //LiveTranscoding.TranscodingUser user = new LiveTranscoding.TranscodingUser();
                    //user.uid = uid;
                    //user.width = encoderConfiguration.dimensions.height;
                    //user.height = encoderConfiguration.dimensions.width;
                    //transcoding.addUser(user);
                    //engine.setLiveTranscoding(transcoding);
                    int publishCode = engine.addPublishStreamUrl(pushRtmpUrl, false);
                    Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d publishCode %d pushRtmpUrl %s", channelId, uid, publishCode, pushRtmpUrl));
                    if(listener != null){
                        listener.onJoinSuccess(uid);
                    }
                }

                @Override
                public void onUserJoined(String channelId, int uid) {
                    if(listener != null){
                        listener.onUserJoined(channelId,uid);
                    }
                }

            };
            publishChannelId = channelId;
            ChannelMediaOptions option = new ChannelMediaOptions();
            option.autoSubscribeAudio = true;
            option.autoSubscribeVideo = true;
            int ret = engine.joinChannel(null, channelId, publishUid, option);
            Log.i(TAG, String.format("joinChannel channel %s ret %d", channelId, ret));
            return;
        }

        if(mRtcChannels.get(channelId) != null){
            return;
        }
        // 1. create channel
        RtcConnection connection = new RtcConnection();
        connection.channelId = channelId;
        connection.localUid = new Random().nextInt(512)+512;
        mRtcChannels.put(channelId, connection);
        if(publish){
            publishUid = connection.localUid;
        }
        engine.setVideoEncoderConfigurationEx(encoderConfiguration, connection);

        // 2. set role
        ChannelMediaOptions channelMediaOptions = new ChannelMediaOptions();
        channelMediaOptions.autoSubscribeAudio = true;
        channelMediaOptions.autoSubscribeVideo = true;

        channelMediaOptions.publishAudioTrack = publish;
        channelMediaOptions.publishCameraTrack = publish;
        channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        int ret = engine.joinChannelEx(null, connection, channelMediaOptions, new IRtcEngineEventHandler() {
            @Override
            public void onError(int err) {
                super.onError(err);
                Log.e(TAG, String.format("onChannelError code %d", err));
                if (listener != null) {
                    listener.onError(err,
                            err == ErrorCode.ERR_INVALID_TOKEN ? "invalid token -- channelId " + channelId : "");
                }
            }

            @Override
            public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
                super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
                Log.e(TAG, String.format("onFirstRemoteVideoFrame uid=%d, width=%d, height=%d", uid, width, height));
                Runnable runnable = firstVideoFramePendingRuns.get(uid);
                if(runnable != null){
                    runnable.run();
                    firstVideoFramePendingRuns.remove(uid);
                }
            }

            @Override
            public void onEncryptionError(ENCRYPTION_ERROR_TYPE errorType) {
                super.onEncryptionError(errorType);
                Log.e(TAG, String.format("onEncryptionError errorType %d", errorType));
            }

            @Override
            public void onPermissionError(PERMISSION permission) {
                super.onPermissionError(permission);
                Log.e(TAG, String.format("onPermissionError permission %d", permission));
            }

            @Override
            public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached) {
                super.onStreamMessageError(uid, streamId, error, missed, cached);
                Log.e(TAG, String.format("onStreamMessageError error %d", error));
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                super.onJoinChannelSuccess(channel, uid, elapsed);
                if (publish) {
                    LiveTranscoding transcoding = new LiveTranscoding();
                    LiveTranscoding.TranscodingUser user = new LiveTranscoding.TranscodingUser();
                    user.uid = uid;
                    user.width = encoderConfiguration.dimensions.height;
                    user.height = encoderConfiguration.dimensions.width;
                    transcoding.addUser(user);
                    engine.setLiveTranscoding(transcoding);
                    int publishCode = engine.addPublishStreamUrl(pushRtmpUrl, false);
                    Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d publishCode %d", channelId, uid, publishCode));
                }
                if (listener != null) {
                    listener.onJoinSuccess(uid);
                }
            }

            @Override
            public void onRtmpStreamingStateChanged(String url, RTMP_STREAM_PUBLISH_STATE state, RTMP_STREAM_PUBLISH_ERROR errCode) {
                super.onRtmpStreamingStateChanged(url, state, errCode);
                Log.i(TAG, String.format("onRtmpStreamingStateChanged url=%s, state=%d, errorCode=%d", url, state, errCode));
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                super.onUserJoined(uid, elapsed);
                if (listener != null) {
                    listener.onUserJoined(channelId, uid);
                }
            }

            @Override
            public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
                super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
                Log.e(TAG, String.format("onRemoteVideoStateChanged uid=%d, state=%d, reason=%d", uid, state, reason));

                if (state == Constants.REMOTE_VIDEO_STATE_PLAYING) {
                    Runnable runnable = firstVideoFramePendingRuns.get(uid);
                    if (runnable != null) {
                        runnable.run();
                        firstVideoFramePendingRuns.remove(uid);
                    }
                }
            }

            @Override
            public void onLocalVideoStats(LocalVideoStats stats) {
                super.onLocalVideoStats(stats);
                Log.e(TAG, String.format("onLocalVideoStats stats=%s", stats.toString()));
            }

            @Override
            public void onRemoteVideoStats(RemoteVideoStats stats) {
                super.onRemoteVideoStats(stats);
                Log.e(TAG, String.format("onRemoteVideoStats stats=%s", stats.toString()));

            }

            @Override
            public void onLeaveChannel(RtcStats stats) {
                super.onLeaveChannel(stats);
                Log.e(TAG, String.format("joinChannelEx onLeaveChannel stats=%s", stats.toString()));
            }
        });
        Log.i(TAG, String.format("joinChannel channel %s ret %d", channelId, ret));
    }

    public void renderRemoteVideo(FrameLayout container, String channelId, int uid, Runnable firstFrame){
        if (engine == null) {
            return;
        }
        if(mRtcChannels.get(channelId) == null){
            return;
        }
        // 4. render video
        View videoView = new SurfaceView(container.getContext());
        container.addView(videoView);
        firstVideoFramePendingRuns.put(uid, firstFrame);
        engine.setupRemoteVideoEx(new VideoCanvas(videoView, RENDER_MODE_HIDDEN, uid), mRtcChannels.get(channelId));
    }


    public void leaveChannelExcept(String channelId){
        if (engine == null) {
            return;
        }
        List<String> keys = new ArrayList<>(mRtcChannels.keySet());
        for (String key : keys) {
            if(!key.equals(channelId)){
                engine.leaveChannelEx(mRtcChannels.get(key));
                mRtcChannels.remove(key);
            }
        }
        if(!channelId.equals(publishChannelId)){
            engine.leaveChannel();
        }
    }


    public IMediaPlayer createPlayer(){
        if(engine == null){
            return null;
        }
        return engine.createMediaPlayer();
    }

    public void release() {
        publishChannelListener = null;
        mRtcChannels.clear();
        firstVideoFramePendingRuns.clear();

        Set<String> keys = mRtcChannels.keySet();
        for (String key : keys) {
            engine.leaveChannelEx(mRtcChannels.get(key));
        }
        engine.leaveChannel();

        if (engine != null) {
            RtcEngine.destroy();
        }
    }

    private static String getPushRtmpUrl(String pushName) {
        return UrlManager.sUrl.getPushUrl(pushName);
    }

    public void switchCamera() {
        if(engine == null){
            return;
        }
        engine.switchCamera();
        if(sCameraDirection == CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT){
            sCameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_REAR;
        }else {
            sCameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT;
        }
        if(sCameraDirection == CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT){
            encoderConfiguration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED;
        }else{
            encoderConfiguration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED;
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
    }
}
