package io.agora.superapp.manager;

import android.content.Context;
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

import static io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_640x360;

public class RtcManager {
    private static final String TAG = "RtcManager";
    private static final int LOCAL_RTC_UID = 0;
    private static final String AGORA_CDN_CHANNEL_PUSH_PREFIX = "rtmp://webdemo-push.agora.io/lbhd/%s";
    private static final String AGORA_CDN_CHANNEL_PULL_PREFIX = "http://webdemo-pull.agora.io/lbhd/%s.flv";

    private static CameraCapturerConfiguration.CAMERA_DIRECTION currCameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT;
    public static boolean isMuteLocalAudio = false;

    private volatile boolean isInitialized = false;

    private RtcEngine engine;

    public static final VideoEncoderConfiguration.VideoDimensions[] RESOLUTIONS_PK_HOST = {
            VideoEncoderConfiguration.VD_120x120,
            VideoEncoderConfiguration.VD_160x120,
            VideoEncoderConfiguration.VD_180x180,
            VideoEncoderConfiguration.VD_240x180,
            VideoEncoderConfiguration.VD_320x180,
            VideoEncoderConfiguration.VD_240x240,
            VideoEncoderConfiguration.VD_320x240,
            VideoEncoderConfiguration.VD_424x240,
            VideoEncoderConfiguration.VD_360x360,
            VideoEncoderConfiguration.VD_480x360,
            VideoEncoderConfiguration.VD_640x360,
            VideoEncoderConfiguration.VD_480x480,
            VideoEncoderConfiguration.VD_640x480,
            VideoEncoderConfiguration.VD_840x480,
            VideoEncoderConfiguration.VD_960x720,
            VideoEncoderConfiguration.VD_1280x720,
            VideoEncoderConfiguration.VD_1920x1080,
            VideoEncoderConfiguration.VD_2540x1440,
            VideoEncoderConfiguration.VD_3840x2160
    };
    public static final VideoEncoderConfiguration.FRAME_RATE[] FRAME_RATES_PK_HOST = {
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_1,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_7,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_10,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_60,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
    };

    public static final VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
            VD_640x360,
            FRAME_RATE_FPS_15,
            700,
            ORIENTATION_MODE_FIXED_PORTRAIT
    );

    private LiveTranscoding liveTranscoding = new LiveTranscoding();
    private int canvas_width = 480;
    private int canvas_height = 640;

    private volatile int localUid = 0;
    private Runnable pendingDirectCDNStoppedRun = null;
    private final IDirectCdnStreamingEventHandler iDirectCdnStreamingEventHandler = new IDirectCdnStreamingEventHandler() {
        @Override
        public void onDirectCdnStreamingStateChanged(DirectCdnStreamingState directCdnStreamingState, DirectCdnStreamingError directCdnStreamingError, String s) {
            Log.d(TAG, String.format("Stream Publish(DirectCdnStreaming): onDirectCdnStreamingStateChanged directCdnStreamingState=%s directCdnStreamingError=%s", directCdnStreamingState.toString(), directCdnStreamingError.toString()));
            switch (directCdnStreamingState){
                case STOPPED:
                    if(pendingDirectCDNStoppedRun != null){
                        pendingDirectCDNStoppedRun.run();
                        pendingDirectCDNStoppedRun = null;
                    }
                    break;
            }
        }

        @Override
        public void onDirectCdnStreamingStats(DirectCdnStreamingStats directCdnStreamingStats) {

        }
    };

    private Runnable pendingStreamUnpublishedlRun = null;
    private Runnable pendingJoinChannelRun = null;

    private IMediaPlayer mMediaPlayer = null;
    private IMediaPlayerObserver mediaPlayerObserver;

    public void init(Context context, String appId, OnInitializeListener listener) {
        if (isInitialized) {
            return;
        }
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
                public void onStreamUnpublished(String url) {
                    super.onStreamUnpublished(url);
                    Log.d(TAG, String.format("Stream Publish: onStreamUnpublished url=%s", url));
                    LeaveChannelOptions leaveChannelOptions = new LeaveChannelOptions();
                    leaveChannelOptions.stopMicrophoneRecording = false;
                    engine.leaveChannel(leaveChannelOptions);
                    engine.startPreview();
                    if (pendingStreamUnpublishedlRun != null) {
                        pendingStreamUnpublishedlRun.run();
                        pendingStreamUnpublishedlRun = null;
                    }
                }

                @Override
                public void onStreamPublished(String url, int error) {
                    super.onStreamPublished(url, error);
                    Log.d(TAG, String.format("Stream Publish: onStreamPublished url=%s error=%d", url, error));
                }

                @Override
                public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                    super.onJoinChannelSuccess(channel, uid, elapsed);
                    localUid = uid;
                    if (pendingJoinChannelRun != null) {
                        pendingJoinChannelRun.run();
                        pendingJoinChannelRun = null;
                    }
                }

                @Override
                public void onUserJoined(int uid, int elapsed) {
                    super.onUserJoined(uid, elapsed);
                    if (listener != null) {
                        listener.onUserJoined(uid);
                    }
                    LiveTranscoding.TranscodingUser user1 = new LiveTranscoding.TranscodingUser();
                    user1.x = canvas_width / 2;
                    user1.y = 0;
                    user1.width = canvas_width / 2;
                    user1.height = canvas_height / 2;
                    user1.uid = uid;
                    user1.zOrder = 1;
                    liveTranscoding.addUser(user1);
                    engine.setLiveTranscoding(liveTranscoding);
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
            engine.disableAudio();
            engine.disableVideo();

            canvas_height = Math.max(encoderConfiguration.dimensions.height, encoderConfiguration.dimensions.width);
            canvas_width = Math.min(encoderConfiguration.dimensions.height, encoderConfiguration.dimensions.width);
            engine.setVideoEncoderConfiguration(encoderConfiguration);
            engine.setDirectCdnStreamingVideoConfiguration(encoderConfiguration);

            engine.setParameters("{\"rtc.audio.opensl.mode\": 0}");

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

    public void renderLocalVideo(FrameLayout container) {
        if (engine == null) {
            return;
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

    public void startDirectCDNStreaming(String channelId) {
        if (engine == null) {
            return;
        }
        engine.enableAudio();
        engine.enableVideo();
        engine.enableLocalAudio(!isMuteLocalAudio);

        engine.setDirectCdnStreamingVideoConfiguration(encoderConfiguration);
        DirectCdnStreamingMediaOptions directCdnStreamingMediaOptions = new DirectCdnStreamingMediaOptions();
        directCdnStreamingMediaOptions.publishCameraTrack = true;
        directCdnStreamingMediaOptions.publishMicrophoneTrack = true;

        String url = String.format(Locale.US, AGORA_CDN_CHANNEL_PUSH_PREFIX, channelId);
        Log.d(TAG, "Stream Publish(DirectCdnStreaming): startDirectCDNStreaming url=" + url);
        engine.startDirectCdnStreaming(iDirectCdnStreamingEventHandler, url, directCdnStreamingMediaOptions);
    }

    public void stopDirectCDNStreaming(Runnable onDirectCDNStopped) {
        if (engine == null) {
            return;
        }
        engine.disableVideo();
        engine.disableVideo();

        pendingDirectCDNStoppedRun = onDirectCDNStopped;
        engine.stopDirectCdnStreaming();
    }

    public void startRtcStreaming(String channelId, boolean transcoding) {
        if (engine == null) {
            return;
        }
        engine.enableAudio();
        engine.enableVideo();
        engine.enableLocalAudio(!isMuteLocalAudio);

        ChannelMediaOptions channelMediaOptions = new ChannelMediaOptions();
        channelMediaOptions.publishAudioTrack = true;
        channelMediaOptions.publishCameraTrack = true;
        channelMediaOptions.clientRoleType = CLIENT_ROLE_BROADCASTER;
        engine.joinChannel(null, channelId, (int) (System.currentTimeMillis() & 0xFFFF), channelMediaOptions);
        if(transcoding){
            pendingJoinChannelRun = new Runnable() {
                @Override
                public void run() {
                    liveTranscoding = new LiveTranscoding();
                    liveTranscoding.width = canvas_width;
                    liveTranscoding.height = canvas_height;
                    liveTranscoding.videoBitrate = encoderConfiguration.bitrate;
                    liveTranscoding.videoFramerate = encoderConfiguration.frameRate;

                    LiveTranscoding.TranscodingUser user = new LiveTranscoding.TranscodingUser();
                    user.uid = localUid;
                    user.x = user.y = 0;
                    user.width = canvas_width;
                    user.height = canvas_height;
                    liveTranscoding.addUser(user);
                    engine.setLiveTranscoding(liveTranscoding);
                    engine.addPublishStreamUrl(String.format(Locale.US, AGORA_CDN_CHANNEL_PUSH_PREFIX, channelId), true);
                }
            };
        }
    }

    public void stopRtcStreaming(String channelId, Runnable leaveChannel) {
        if (engine == null) {
            return;
        }
        engine.disableVideo();
        engine.disableVideo();
        pendingStreamUnpublishedlRun = leaveChannel;
        pendingJoinChannelRun = null;
        localUid = 0;
        if(leaveChannel == null){
            engine.leaveChannel();
        }else{
            int ret = engine.removePublishStreamUrl(String.format(Locale.US, AGORA_CDN_CHANNEL_PUSH_PREFIX, channelId));
            Log.d(TAG, "Stream Publish: stopRtcStreaming removePublishStreamUrl ret=" + ret );
        }
        engine.stopPreview();
    }

    public void renderRemoteVideo(FrameLayout container, int uid) {
        if (engine == null) {
            return;
        }
        // 4. render video
        TextureView videoView = new TextureView(container.getContext());
        container.addView(videoView);
        engine.setupRemoteVideo(new VideoCanvas(videoView, RENDER_MODE_HIDDEN, uid));
    }

    public void release() {
        stopPlayer();
        if(mMediaPlayer != null){
            mMediaPlayer.destroy();
            mMediaPlayer.unRegisterPlayerObserver(mediaPlayerObserver);
            mMediaPlayer = null;
        }

        pendingDirectCDNStoppedRun = null;
        pendingStreamUnpublishedlRun = null;
        if (engine != null) {
            engine.leaveChannel();
            engine.stopDirectCdnStreaming();
            engine.stopPreview();
            RtcEngine.destroy();
            engine = null;
        }
    }

    public void renderPlayerView(FrameLayout container, Runnable firstFrameRun) {
        if (engine == null) {
            return;
        }
        if(mMediaPlayer == null){
            mMediaPlayer = engine.createMediaPlayer();
            mediaPlayerObserver = new IMediaPlayerObserver() {
                @Override
                public void onPlayerStateChanged(io.agora.mediaplayer.Constants.MediaPlayerState mediaPlayerState, io.agora.mediaplayer.Constants.MediaPlayerError mediaPlayerError) {
                    Log.d(TAG, "MediaPlayer onPlayerStateChanged -- state=" + mediaPlayerState + ", error=" + mediaPlayerError);
                    if (mediaPlayerState == io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                        if (mMediaPlayer != null) {
                            mMediaPlayer.play();
                        }

                    }


                }

                @Override
                public void onPositionChanged(long l) {

                }

                @Override
                public void onPlayerEvent(io.agora.mediaplayer.Constants.MediaPlayerEvent mediaPlayerEvent, long l, String s) {
                    if(mediaPlayerEvent == io.agora.mediaplayer.Constants.MediaPlayerEvent.PLAYER_EVENT_FIRST_DISPLAYED){
                        if (firstFrameRun != null) {
                            firstFrameRun.run();
                        }
                    }
                }

                @Override
                public void onMetaData(io.agora.mediaplayer.Constants.MediaPlayerMetadataType mediaPlayerMetadataType, byte[] bytes) {

                }

                @Override
                public void onPlayBufferUpdated(long l) {

                }

                @Override
                public void onPreloadEvent(String s, io.agora.mediaplayer.Constants.MediaPlayerPreloadEvent mediaPlayerPreloadEvent) {

                }

                @Override
                public void onCompleted() {

                }

                @Override
                public void onAgoraCDNTokenWillExpire() {

                }

                @Override
                public void onPlayerSrcInfoChanged(SrcInfo srcInfo, SrcInfo srcInfo1) {

                }

                @Override
                public void onPlayerInfoUpdated(PlayerUpdatedInfo playerUpdatedInfo) {

                }

            };
            mMediaPlayer.registerPlayerObserver(mediaPlayerObserver);
            engine.setDefaultAudioRoutetoSpeakerphone(true);
        }

        SurfaceView surfaceView = new SurfaceView(container.getContext());
        surfaceView.setZOrderMediaOverlay(false);
        container.addView(surfaceView);

        engine.setupLocalVideo(new VideoCanvas(surfaceView, Constants.RENDER_MODE_HIDDEN,
                Constants.VIDEO_MIRROR_MODE_AUTO,
                Constants.VIDEO_SOURCE_MEDIA_PLAYER,
                mMediaPlayer.getMediaPlayerId(),
                LOCAL_RTC_UID
        ));
    }

    public void openPlayerSrc(String channelId, boolean isCdn){
        if(mMediaPlayer == null){
            return;
        }
        String url = String.format(Locale.US, AGORA_CDN_CHANNEL_PULL_PREFIX, channelId);
        if(isCdn){
            mMediaPlayer.stop();
            mMediaPlayer.openWithAgoraCDNSrc(url, LOCAL_RTC_UID);
        }else{
            mMediaPlayer.stop();
            mMediaPlayer.open(url, LOCAL_RTC_UID);
        }
    }

    public void stopPlayer(){
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
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
