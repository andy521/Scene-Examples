package io.agora.livepk.manager;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcChannelEventHandler;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcChannel;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.live.LiveTranscoding;
import io.agora.rtc.models.ChannelMediaOptions;
import io.agora.rtc.models.ClientRoleOptions;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static io.agora.rtc.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE;
import static io.agora.rtc.video.VideoEncoderConfiguration.VD_640x360;

public class RtcManager {
    private static final String TAG = "RtcManager";
    private static final int LOCAL_RTC_UID = 0;

    private volatile boolean isInitialized = false;
    private RtcEngine engine;
    private final Map<String, RtcChannel> mRtcChannels = new HashMap<>();
    private final Map<Integer, Runnable> firstVideoFramePendingRuns = new HashMap<>();
    private Context mContext;

    private final VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
            VD_640x360,
            FRAME_RATE_FPS_15,
            700,
            ORIENTATION_MODE_ADAPTIVE
    );

    public void init(Context context, String appId, OnInitializeListener listener){
        if(isInitialized){
            return;
        }
        mContext = context;
        try {
            // 0. create engine
            engine = RtcEngine.create(mContext.getApplicationContext(), appId, new IRtcEngineEventHandler() {
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
                    Runnable runnable = firstVideoFramePendingRuns.get(LOCAL_RTC_UID);
                    if(runnable != null){
                        runnable.run();
                        firstVideoFramePendingRuns.remove(LOCAL_RTC_UID);
                    }
                }

                @Override
                public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                    super.onJoinChannelSuccess(channel, uid, elapsed);
                }
            });
            engine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            engine.setAudioProfile(Constants.AUDIO_PROFILE_SPEECH_STANDARD, Constants.AUDIO_SCENARIO_CHATROOM_ENTERTAINMENT);
            engine.setDefaultAudioRoutetoSpeakerphone(true);
            // 在JoinChannelSuccess后调用
            // engine.setParameters("{\"che.video.lowBitRateStreamParameter\": \"{\\\"width\\\":270,\\\"height\\\":480,\\\"frameRate\\\":15,\\\"bitRate\\\":400}\"}");
            engine.setParameters("{\"che.video.retransDetectEnable\":true}");
            engine.setParameters("{\"che.video.camera.face_detection\":false}");
            engine.setParameters("{\"che.video.captureFpsLowPower\":true}");
            engine.setParameters("{\"che.video.android_zero_copy_mode\":2}");
            engine.setParameters("{\"che.video.setQuickVideoHighFec\":true}");
            engine.setParameters("{\"rtc.enable_quick_rexfer_keyframe\":true}");
            engine.setParameters("{\"rtc.enable_audio_rsfec_in_video\":true}");
            engine.setParameters("{\"che.audio.opensl\":true}");
            engine.setParameters("{\"che.audio.specify.codec\":\"OPUSFB\"}");

            engine.enableVideo();
            engine.enableAudio();
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
        View videoView = RtcEngine.CreateTextureView(mContext);
        container.addView(videoView);
        firstVideoFramePendingRuns.put(LOCAL_RTC_UID, firstFrame);
        engine.setupLocalVideo(new VideoCanvas(videoView, RENDER_MODE_HIDDEN, LOCAL_RTC_UID));
        engine.startPreview();
    }

    public void joinChannel(String channelId, boolean publish, OnChannelListener listener){
        if (engine == null) {
            return;
        }
        if(mRtcChannels.get(channelId) != null){
            return;
        }
        // 1. create channel
        RtcChannel rtcChannel = engine.createRtcChannel(channelId);
        // 2. set role
        ClientRoleOptions options = new ClientRoleOptions();
        options.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY;
        rtcChannel.setClientRole(Constants.CLIENT_ROLE_BROADCASTER, options);
        mRtcChannels.put(channelId, rtcChannel);

        rtcChannel.setRtcChannelEventHandler(new IRtcChannelEventHandler() {
            @Override
            public void onChannelError(RtcChannel rtcChannel, int err) {
                super.onChannelError(rtcChannel, err);
                Log.e(TAG, String.format("onChannelError code %d", err));
                if(listener != null){
                    listener.onError(err,
                            err == IRtcEngineEventHandler.ErrorCode.ERR_INVALID_TOKEN ? "invalid token -- channelId " + channelId : "");
                }
            }

            @Override
            public void onJoinChannelSuccess(RtcChannel rtcChannel, int uid, int elapsed) {
                super.onJoinChannelSuccess(rtcChannel, uid, elapsed);
                engine.setParameters("{\"che.video.lowBitRateStreamParameter\": \"{\\\"width\\\":270,\\\"height\\\":480,\\\"frameRate\\\":15,\\\"bitRate\\\":400}\"}");
                if(publish){
                    LiveTranscoding transcoding = new LiveTranscoding();
                    LiveTranscoding.TranscodingUser user = new LiveTranscoding.TranscodingUser();
                    user.uid = uid;
                    user.width = encoderConfiguration.dimensions.height;
                    user.height = encoderConfiguration.dimensions.width;
                    transcoding.addUser(user);

                    rtcChannel.setLiveTranscoding(transcoding);
                    int publishCode = rtcChannel.addPublishStreamUrl(getPushRtmpUrl(channelId), true);
                    Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d publishCode %d", channelId, uid, publishCode));

                }
                if(listener != null){
                    listener.onJoinSuccess(uid);
                }
            }

            @Override
            public void onRtmpStreamingEvent(RtcChannel rtcChannel, String url, int errCode) {
                super.onRtmpStreamingEvent(rtcChannel, url, errCode);
                Log.i(TAG, String.format("onRtmpStreamingEvent url=%s, errorCode=%d", url, errCode));
            }

            @Override
            public void onRtmpStreamingStateChanged(RtcChannel rtcChannel, String url, int state, int errCode) {
                super.onRtmpStreamingStateChanged(rtcChannel, url, state, errCode);
                Log.i(TAG, String.format("onRtmpStreamingStateChanged url=%s, state=%d, errorCode=%d", url, state, errCode));
            }

            @Override
            public void onUserJoined(RtcChannel rtcChannel, int uid, int elapsed) {
                super.onUserJoined(rtcChannel, uid, elapsed);
                if(listener != null){
                    listener.onUserJoined(channelId, uid);
                }
            }

            @Override
            public void onRemoteVideoStateChanged(RtcChannel rtcChannel, int uid, int state, int reason, int elapsed) {
                super.onRemoteVideoStateChanged(rtcChannel, uid, state, reason, elapsed);
                Log.d(TAG, "onRemoteVideoStateChanged uid=" + uid + ", state=" + state);
                if(state == Constants.REMOTE_VIDEO_STATE_DECODING){
                    Runnable runnable = firstVideoFramePendingRuns.get(uid);
                    if(runnable != null){
                        runnable.run();
                        firstVideoFramePendingRuns.remove(uid);
                    }
                }
            }

        });

        ChannelMediaOptions mediaOptions = new ChannelMediaOptions();
        mediaOptions.autoSubscribeAudio = true;
        mediaOptions.autoSubscribeVideo = true;
        mediaOptions.publishLocalAudio = publish;
        mediaOptions.publishLocalVideo = publish;
        // 3. Join channel
        int ret = rtcChannel.joinChannel("", "", 0, mediaOptions);
        Log.i(TAG, String.format("joinChannel channel %s ret %d", channelId, ret));
    }

    public void renderRemoteVideo(FrameLayout container, String channelId, int uid, Runnable firstFrame){
        if (engine == null) {
            return;
        }
        // 4. render video
        View videoView = RtcEngine.CreateRendererView(mContext);
        container.addView(videoView);
        firstVideoFramePendingRuns.put(uid, firstFrame);
        engine.setupRemoteVideo(new VideoCanvas(videoView, RENDER_MODE_HIDDEN, channelId, uid));

    }

    public void leaveChannel(String channelId){
        if (engine == null) {
            return;
        }
        RtcChannel rtcChannel = mRtcChannels.get(channelId);
        if(rtcChannel != null){
            return;
        }
        rtcChannel.leaveChannel();
        mRtcChannels.remove(channelId);
    }

    public void leaveChannelExcept(String channelId){
        if (engine == null) {
            return;
        }
        List<String> keys = new ArrayList<>(mRtcChannels.keySet());
        for (String key : keys) {
            if(!key.equals(channelId)){
                mRtcChannels.get(key).leaveChannel();
                mRtcChannels.remove(key);
            }
        }
    }

    public void release() {
        Set<String> keys = mRtcChannels.keySet();
        for (String key : keys) {
            mRtcChannels.get(key).leaveChannel();
        }
        mRtcChannels.clear();
        if (engine != null) {
            RtcEngine.destroy();
        }
    }

    private static String getPushRtmpUrl(String pushName) {
        return String.format(Locale.US, "rtmp://webdemo-push.agora.io/lbhd/%s", pushName);
    }

    public void switchCamera() {
        if(engine != null){
            engine.switchCamera();
        }
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
