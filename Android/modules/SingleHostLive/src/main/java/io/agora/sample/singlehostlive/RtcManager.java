package io.agora.sample.singlehostlive;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.agora.mediaplayer.IMediaPlayer;
import io.agora.mediaplayer.IMediaPlayerObserver;
import io.agora.mediaplayer.data.PlayerUpdatedInfo;
import io.agora.mediaplayer.data.SrcInfo;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.DirectCdnStreamingError;
import io.agora.rtc2.DirectCdnStreamingMediaOptions;
import io.agora.rtc2.DirectCdnStreamingState;
import io.agora.rtc2.DirectCdnStreamingStats;
import io.agora.rtc2.IDirectCdnStreamingEventHandler;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.LeaveChannelOptions;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.live.LiveTranscoding;
import io.agora.rtc2.video.CameraCapturerConfiguration;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

import static io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE;
import static io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_640x360;

public class RtcManager {
    private static final String TAG = "RtcManager";
    private static final int LOCAL_RTC_UID = 0;

    private static CameraCapturerConfiguration.CAMERA_DIRECTION currCameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT;
    public static boolean isMuteLocalAudio = false;

    private volatile boolean isInitialized = false;
    private Context mContext;

    private RtcEngine engine;

    private final VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
            VD_640x360,
            FRAME_RATE_FPS_15,
            700,
            ORIENTATION_MODE_FIXED_PORTRAIT
    );

    private volatile int localUid = 0;


    public void init(Context context, String appId, OnInitializeListener listener) {
        if (isInitialized) {
            return;
        }
        mContext = context;
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = context.getApplicationContext();
            config.mAppId = appId;
            config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
            config.mEventHandler = new IRtcEngineEventHandler() {
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
                    }
                }

                @Override
                public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                    super.onJoinChannelSuccess(channel, uid, elapsed);
                    localUid = uid;
                }

                @Override
                public void onUserJoined(int uid, int elapsed) {
                    super.onUserJoined(uid, elapsed);
                    if (listener != null) {
                        listener.onUserJoined(uid);
                    }
                }

                @Override
                public void onUserOffline(int uid, int reason) {
                    super.onUserOffline(uid, reason);
                    if (listener != null) {
                        listener.onUserLeaved(uid, reason);
                    }
                }
            };
            engine = RtcEngine.create(config);
            engine.setDefaultAudioRoutetoSpeakerphone(true);
            engine.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_BROADCASTER);
            engine.enableVideo();
            engine.enableAudio();
            engine.enableLocalAudio(!isMuteLocalAudio);

            engine.setVideoEncoderConfiguration(encoderConfiguration);
            isInitialized = true;
        } catch (Exception e) {
            if (listener != null) {
                listener.onError(-1, "RtcEngine create exception : " + e.toString());
            }
        }
    }

    public void setCurrCameraDirection(CameraCapturerConfiguration.CAMERA_DIRECTION direction) {
        if (currCameraDirection != direction) {
            currCameraDirection = direction;
            if (engine != null) {
                engine.switchCamera();
            }
        }
    }

    public void renderLocalVideo(FrameLayout container, Runnable firstFrame) {
        if (engine == null) {
            return;
        }
        if(firstFrame != null){
            firstFrame.run();
        }
        engine.setCameraCapturerConfiguration(new CameraCapturerConfiguration(currCameraDirection));
        View videoView = new SurfaceView(container.getContext());
        container.addView(videoView);
        int ret = engine.setupLocalVideo(new VideoCanvas(videoView, RENDER_MODE_HIDDEN, LOCAL_RTC_UID));
        Log.d(TAG, "setupLocalVideo ret=" + ret);
        ret = engine.startPreview();
        Log.d(TAG, "startPreview ret=" + ret);
    }

    public void switchCamera() {
        if (engine == null) {
            return;
        }
        engine.switchCamera();
        if (currCameraDirection == CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT) {
            currCameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_REAR;
        } else {
            currCameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT;
        }
    }


    public void joinChannel(String channelId, String uid, boolean publish) {
        if (engine == null) {
            return;
        }
        engine.enableLocalAudio(publish);
        engine.enableLocalVideo(publish);

        ChannelMediaOptions channelMediaOptions = new ChannelMediaOptions();
        channelMediaOptions.publishAudioTrack = publish;
        channelMediaOptions.publishCameraTrack = publish;
        channelMediaOptions.clientRoleType = publish? CLIENT_ROLE_BROADCASTER: CLIENT_ROLE_AUDIENCE;
        int _uid = (int) (System.currentTimeMillis() & 0xFFFF);
        if(!TextUtils.isEmpty(uid)){
            try {
                _uid = Integer.parseInt(uid);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        int ret = engine.joinChannel(null, channelId, _uid, channelMediaOptions);
        Log.d(TAG, "joinChannel channel=" + channelId + ",uid=" + _uid + ",ret=" + ret);
    }

    public void renderRemoteVideo(FrameLayout container, int uid, Runnable firstFrame) {
        if (engine == null) {
            return;
        }
        if(firstFrame != null){
            firstFrame.run();
        }
        // 4. render video
        TextureView videoView = new TextureView(container.getContext());
        container.addView(videoView);
        engine.setupRemoteVideo(new VideoCanvas(videoView, RENDER_MODE_HIDDEN, uid));
    }

    public void release() {
        if (engine != null) {
            engine.leaveChannel();
            engine.stopDirectCdnStreaming();
            engine.stopPreview();
            RtcEngine.destroy();
            engine = null;
        }
    }

    public void muteLocalAudio(boolean mute){
        if(engine == null){
            return;
        }
        isMuteLocalAudio = mute;
        engine.enableLocalAudio(!mute);
    }

    public interface OnInitializeListener {
        void onError(int code, String message);

        void onSuccess();

        void onUserJoined(int uid);

        void onUserLeaved(int uid, int reason);
    }

}
