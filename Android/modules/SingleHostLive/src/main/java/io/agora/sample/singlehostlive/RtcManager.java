package io.agora.sample.singlehostlive;

import android.content.Context;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcChannelEventHandler;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcChannel;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.mediaio.AgoraSurfaceView;
import io.agora.rtc.mediaio.AgoraTextureView;
import io.agora.rtc.mediaio.IVideoSink;
import io.agora.rtc.mediaio.MediaIO;
import io.agora.rtc.models.ChannelMediaOptions;
import io.agora.rtc.models.ClientRoleOptions;
import io.agora.rtc.video.CameraCapturerConfiguration;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static io.agora.rtc.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;
import static io.agora.rtc.video.VideoEncoderConfiguration.VD_640x360;

public class RtcManager {
    private static final String TAG = "RtcManager";
    private static final int LOCAL_RTC_UID = 0;

    private volatile boolean isInitialized = false;
    private RtcEngine engine;
    private final Map<String, RtcChannel> mRtcChannels = new HashMap<>();
    private final Map<Integer, Runnable> firstVideoFramePendingRuns = new HashMap<>();
    private Context mContext;
    private OnChannelListener publishChannelListener;
    private String publishChannelId;

    private static CameraCapturerConfiguration.CAMERA_DIRECTION cameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT;

    private final VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
            VD_640x360,
            FRAME_RATE_FPS_15,
            700,
            ORIENTATION_MODE_FIXED_PORTRAIT
    );

    public void init(Context context, String appId, OnInitializeListener listener) {
        if (isInitialized) {
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

            engine.setAudioProfile(Constants.AUDIO_PROFILE_SPEECH_STANDARD, Constants.AUDIO_SCENARIO_CHATROOM_ENTERTAINMENT);
            engine.setDefaultAudioRoutetoSpeakerphone(true);
            engine.enableDualStreamMode(false);

            engine.enableVideo();
            engine.enableAudio();
            encoderConfiguration.mirrorMode = Constants.VIDEO_MIRROR_MODE_ENABLED;
            engine.setVideoEncoderConfiguration(encoderConfiguration);

            engine.setCameraCapturerConfiguration(new CameraCapturerConfiguration(new CameraCapturerConfiguration.CaptureDimensions(encoderConfiguration.dimensions.width, encoderConfiguration.dimensions.height), cameraDirection));
            isInitialized = true;
        } catch (Exception e) {
            if (listener != null) {
                listener.onError(-1, "RtcEngine create exception : " + e.toString());
            }
        }
    }

    public void renderLocalVideo(FrameLayout container, Runnable firstFrame) {
        if (engine == null) {
            return;
        }
        View videoView = RtcEngine.CreateRendererView(mContext);
        container.addView(videoView);
        firstVideoFramePendingRuns.put(LOCAL_RTC_UID, firstFrame);
        engine.setupLocalVideo(new VideoCanvas(videoView, RENDER_MODE_HIDDEN, LOCAL_RTC_UID));
        engine.startPreview();
    }

    public void joinChannel(String channelId, String uid, boolean publish, OnChannelListener listener) {
        if (engine == null) {
            return;
        }
        if (publish) {
            ChannelMediaOptions options = new ChannelMediaOptions();
            options.publishLocalAudio = true;
            options.publishLocalVideo = true;

            ClientRoleOptions roleOptions = new ClientRoleOptions();
            roleOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY;
            engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER, roleOptions);

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
            };
            int _uid = LOCAL_RTC_UID;
            if(!TextUtils.isEmpty(uid)){
                try {
                    _uid = Integer.parseInt(uid);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            engine.joinChannel(null, channelId, null, _uid, options);

            return;
        }

        if (mRtcChannels.get(channelId) != null) {
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
                if (listener != null) {
                    listener.onError(err,
                            err == IRtcEngineEventHandler.ErrorCode.ERR_INVALID_TOKEN ? "invalid token -- channelId " + channelId : "");
                }
            }

            @Override
            public void onJoinChannelSuccess(RtcChannel rtcChannel, int uid, int elapsed) {
                super.onJoinChannelSuccess(rtcChannel, uid, elapsed);
                if (listener != null) {
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
                Log.d(TAG, "joinChannel onUserJoined uid=" + uid);
                if (listener != null) {
                    listener.onUserJoined(channelId, uid);
                }
            }

            @Override
            public void onRemoteVideoStateChanged(RtcChannel rtcChannel, int uid, int state, int reason, int elapsed) {
                super.onRemoteVideoStateChanged(rtcChannel, uid, state, reason, elapsed);
                Log.d(TAG, "onRemoteVideoStateChanged uid=" + uid + ", state=" + state);
                if (state == Constants.REMOTE_VIDEO_STATE_DECODING) {
                    Runnable runnable = firstVideoFramePendingRuns.get(uid);
                    if (runnable != null) {
                        runnable.run();
                        firstVideoFramePendingRuns.remove(uid);
                    }
                }
            }

            @Override
            public void onRemoteVideoStats(RtcChannel rtcChannel, IRtcEngineEventHandler.RemoteVideoStats stats) {
                super.onRemoteVideoStats(rtcChannel, stats);

            }

            @Override
            public void onStreamMessage(RtcChannel rtcChannel, int uid, int streamId, byte[] data) {
                super.onStreamMessage(rtcChannel, uid, streamId, data);
                Log.d(TAG, "RtcChannel onStreamMessage uid=" + uid + ",streamId=" + streamId + ",data=" + new String(data));

            }

            @Override
            public void onStreamMessageError(RtcChannel rtcChannel, int uid, int streamId, int error, int missed, int cached) {
                super.onStreamMessageError(rtcChannel, uid, streamId, error, missed, cached);
                Log.d(TAG, "RtcChannel onStreamMessageError uid=" + uid + ",streamId=" + streamId + ",error=" + error + ",missed=" + missed + ",cached=" + cached);
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

    public void renderRemoteVideo(FrameLayout container, String channelId, int uid) {
        if (engine == null) {
            return;
        }

        IVideoSink videoView = new AgoraTextureView(container.getContext()) {
            private static final int DISPLAY_FRAME_INDEX = 1;
            private int frameIndex = 0;

            @Override
            public void consumeByteBufferFrame(ByteBuffer buffer, int format, int width, int height, int rotation, long ts) {
                super.consumeByteBufferFrame(buffer, format, width, height, rotation, ts);
                // 渲染完一帧视频帧，非主线程
                if (frameIndex == DISPLAY_FRAME_INDEX) {
                    Log.d(TAG, "consumeTextureFrame first frame threadName=" + Thread.currentThread().getName());
                    //post(() -> setAlpha(1.0f));
                    setAlpha(1.0f);

                }
                if (frameIndex < DISPLAY_FRAME_INDEX + 1) {
                    frameIndex++;
                }
            }
        };

        if (videoView instanceof AgoraTextureView) {
            AgoraTextureView view = (AgoraTextureView) videoView;
            view.setBufferType(MediaIO.BufferType.BYTE_BUFFER);
            view.setPixelFormat(MediaIO.PixelFormat.I420);
            //view.setOpaque(true);
            view.setAlpha(0);
            container.addView(view);
        } else if (videoView instanceof AgoraSurfaceView) {
            AgoraSurfaceView view = (AgoraSurfaceView) videoView;
            view.setBufferType(MediaIO.BufferType.BYTE_BUFFER);
            view.setPixelFormat(MediaIO.PixelFormat.I420);
            view.setZOrderMediaOverlay(true);
            view.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            container.addView(view);
        }

        if (mRtcChannels.get(channelId) != null) {
            mRtcChannels.get(channelId).setRemoteVideoRenderer(uid, videoView);
        } else {
            engine.setRemoteVideoRenderer(uid, videoView);
        }
    }

    public void leaveChannel(String channelId) {
        if (engine == null) {
            return;
        }
        RtcChannel rtcChannel = mRtcChannels.get(channelId);
        if (rtcChannel != null) {
            return;
        }
        rtcChannel.leaveChannel();
        mRtcChannels.remove(channelId);
    }

    public void leaveChannelExcept(String channelId) {
        if (engine == null) {
            return;
        }
        List<String> keys = new ArrayList<>(mRtcChannels.keySet());
        for (String key : keys) {
            if (!key.equals(channelId)) {
                RtcChannel rtcChannel = mRtcChannels.get(key);
                if (rtcChannel != null) {
                    rtcChannel.leaveChannel();
                    mRtcChannels.remove(key);
                }
            }
        }
    }

    public void release() {
        publishChannelListener = null;
        Set<String> keys = mRtcChannels.keySet();
        for (String key : keys) {
            RtcChannel rtcChannel = mRtcChannels.get(key);
            if (rtcChannel != null) {
                rtcChannel.leaveChannel();
            }
        }
        mRtcChannels.clear();
        if (engine != null) {
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
        } else {
            cameraDirection = CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT;
        }
        if (encoderConfiguration.mirrorMode == Constants.VIDEO_MIRROR_MODE_ENABLED) {
            encoderConfiguration.mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED;
        } else {
            encoderConfiguration.mirrorMode = Constants.VIDEO_MIRROR_MODE_ENABLED;
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
