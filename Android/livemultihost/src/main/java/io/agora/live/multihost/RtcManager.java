package io.agora.live.multihost;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static io.agora.rtc.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE;
import static io.agora.rtc.video.VideoEncoderConfiguration.STANDARD_BITRATE;

public class RtcManager {

    private static final String TAG = "RtcManager";

    private volatile boolean isInitialized = false;
    private RtcEngine engine;
    private Context mContext;

    private Map<Integer, Runnable> remoteFirstFrameRun = new HashMap<>();
    private Runnable localFirstFrameRun;
    private int localRenderUid = 0;

    private final VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
            new VideoEncoderConfiguration.VideoDimensions(190, 190),
            FRAME_RATE_FPS_15,
            STANDARD_BITRATE,
            ORIENTATION_MODE_ADAPTIVE
    );

    public void init(Context context, String appId, OnInitializeListener listener) {
        if (isInitialized) {
            return;
        }
        mContext = context;
        try {
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
                    }
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
                        listener.onUserOffline(uid);
                    }
                }

                @Override
                public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                    super.onJoinChannelSuccess(channel, uid, elapsed);
                    if (listener != null) {
                        listener.onJoinChannelSuccess(channel, uid);
                    }
                }

                @Override
                public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
                    super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
                    Runnable runnable = remoteFirstFrameRun.get(uid);
                    if(runnable != null){
                        runnable.run();
                        remoteFirstFrameRun.remove(uid);
                    }
                }

                @Override
                public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
                    super.onFirstLocalVideoFrame(width, height, elapsed);
                    if (localFirstFrameRun != null) {
                        localFirstFrameRun.run();
                        localFirstFrameRun = null;
                    }
                }
            });
            engine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            engine.setParameters("{\"che.video.setQuickVideoHighFec\":true}");
            engine.setParameters("{\"rtc.enable_quick_rexfer_keyframe\":true}");
            engine.setParameters("{\"che.audio.opensl\":true}");
            engine.setParameters("{\"che.video.h264.hwenc\":0}");
            engine.enableVideo();
            engine.enableAudio();
            isInitialized = true;
        } catch (Exception e) {
            if (listener != null) {
                listener.onError(-1, "RtcEngine create exception : " + e.toString());
            }
        }
    }

    public void setClientRole(boolean isBroadCast) {
        if (engine == null) {
            return;
        }
        if (isBroadCast) {
            engine.enableLocalVideo(true);
            engine.enableLocalAudio(true);
            engine.setVideoEncoderConfiguration(encoderConfiguration);
            engine.setDefaultAudioRoutetoSpeakerphone(true);
        } else {
            engine.enableLocalVideo(false);
            engine.enableLocalAudio(false);
        }
        engine.setClientRole(isBroadCast ? Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE);
    }

    public void joinChannel(String channelId, int uid) {
        if (engine == null) {
            return;
        }
        engine.joinChannel("", channelId, "", uid);
    }

    public void leaveChannel() {
        if (engine == null) {
            return;
        }
        engine.leaveChannel();
    }

    public void renderLocalVideo(FrameLayout container, int uid, View renderView) {
        if (engine == null) {
            return;
        }
        container.setDrawingCacheEnabled(true);
        Bitmap drawingCache = container.getDrawingCache();
        Bitmap originBgBitmap = Bitmap.createBitmap(drawingCache);
        container.setDrawingCacheEnabled(false);

        localRenderUid = uid;
        View videoView;
        if(renderView != null){
            videoView = renderView;
        }else{
            videoView = RtcEngine.CreateRendererView(mContext);
            videoView.setTag(TAG);
            container.addView(videoView);
        }

        //FrameLayout overlayer = new FrameLayout(container.getContext());
        //overlayer.setBackground(new BitmapDrawable(originBgBitmap));
        //overlayer.setTag(TAG);
        //container.addView(overlayer);
        //localFirstFrameRun = () -> container.removeView(overlayer);

        engine.setupLocalVideo(new VideoCanvas(videoView, RENDER_MODE_HIDDEN, uid));
        engine.startPreview();
    }

    public void release() {
        remoteFirstFrameRun.clear();
        localFirstFrameRun = null;
        localRenderUid = 0;
        if (engine != null) {
            engine.leaveChannel();
            RtcEngine.destroy();
        }
    }

    public void renderRemoteVideo(FrameLayout container, int uid, View renderView) {
        if (engine == null) {
            return;
        }

        container.setDrawingCacheEnabled(true);
        Bitmap drawingCache = container.getDrawingCache();
        Bitmap originBgBitmap = Bitmap.createBitmap(drawingCache);
        container.setDrawingCacheEnabled(false);

        View videoView;
        if(renderView != null){
            videoView = renderView;
        }else{
            videoView = RtcEngine.CreateRendererView(mContext);
            videoView.setTag(TAG);
            container.addView(videoView);
        }

        //FrameLayout overlayer = new FrameLayout(container.getContext());
        //overlayer.setBackground(new BitmapDrawable(originBgBitmap));
        //overlayer.setTag(TAG);
        //container.addView(overlayer);
        //remoteFirstFrameRun.put(uid, () -> container.removeView(overlayer));

        engine.setupRemoteVideo(new VideoCanvas(videoView, RENDER_MODE_HIDDEN, uid));
    }

    public View cleanVideoView(FrameLayout container, int uid, boolean keep){
        int childCount = container.getChildCount();
        View renderView = null;
        for (int index = childCount - 1; index >= 0; index--) {
            View child = container.getChildAt(index);
            if(TAG.equals(child.getTag())){
                if(keep && child instanceof SurfaceView){
                    renderView = child;
                }else{
                    container.removeViewAt(index);
                }
            }

        }
        if(uid == localRenderUid){
            localFirstFrameRun = null;
        }else{
            remoteFirstFrameRun.remove(uid);
        }
        return renderView;
    }

    public interface OnInitializeListener {
        void onError(int code, String message);

        void onSuccess();

        void onUserJoined(int uid);

        void onUserOffline(int uid);

        void onJoinChannelSuccess(String channel, int uid);

    }

}
