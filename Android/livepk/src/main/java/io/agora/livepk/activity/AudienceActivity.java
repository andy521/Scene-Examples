package io.agora.livepk.activity;

import static io.agora.mediaplayer.Constants.PLAYER_RENDER_MODE_HIDDEN;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.livepk.R;
import io.agora.livepk.databinding.ActivityVideoBinding;
import io.agora.livepk.manager.RtcManager;
import io.agora.livepk.manager.RtmManager;
import io.agora.livepk.manager.UrlManager;
import io.agora.livepk.model.RoomInfo;
import io.agora.livepk.util.UserUtil;
import io.agora.mediaplayer.Constants;
import io.agora.mediaplayer.IMediaPlayer;
import io.agora.mediaplayer.IMediaPlayerObserver;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannelAttribute;

public class AudienceActivity extends DataBindBaseActivity<ActivityVideoBinding> {
    private static final String TAG = "AudienceActivity";

    private static final String EXTRA_ROOM_INFO = "roomInfo";

    private IMediaPlayer agoraMediaPlayerKitA, agoraMediaPlayerKitB;
    private final RtmManager rtmManager = new RtmManager();
    private final RtcManager rtcManager = new RtcManager();
    private RoomInfo mRoomInfo;

    public static Intent launch(Context context, RoomInfo roomInfo) {
        Intent intent = new Intent(context, AudienceActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        return intent;
    }

    @Override
    protected void iniBundle(@NonNull Bundle bundle) {
        mRoomInfo = (RoomInfo)getIntent().getSerializableExtra(EXTRA_ROOM_INFO);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video;
    }

    @Override
    protected void iniView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(mRoomInfo.roomName);
        setStatusBarStyle(true);
        setStatusBarTransparent();

        int userProfileIcon = UserUtil.getUserProfileIcon(mRoomInfo.roomId);

        mDataBinding.ivLoadingBg.setVisibility(View.VISIBLE);
        mDataBinding.ivLoadingBg.setImageResource(userProfileIcon);

        Glide.with(this)
                .load(userProfileIcon)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(mDataBinding.ivRoomAvatar);

        mDataBinding.tvRoomName.setText(String.format(Locale.US, "%s(%s)", mRoomInfo.roomName, mRoomInfo.roomId));

        mDataBinding.startPkButton.setVisibility(View.GONE);
    }

    @Override
    protected void iniListener() {
        mDataBinding.ivClose.setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void iniData() {
        rtcManager.init(this, getAgoraAppId(), new RtcManager.OnInitializeListener() {
            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onSuccess() {

            }
        });
        initRtmManager(mRoomInfo.roomId);
    }

    private void initRtmManager(String roomId) {
        rtmManager.init(this, getAgoraAppId(), new RtmManager.OnInitializeListener() {
            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onSuccess() {
                rtmManager.joinChannel(roomId, new RtmManager.OnChannelListener() {
                    @Override
                    public void onError(int code, String message) {

                    }

                    @Override
                    public void onJoinSuccess() {
                        rtmManager.getChannelAttributes(roomId, new ResultCallback<List<RtmChannelAttribute>>() {
                            @Override
                            public void onSuccess(List<RtmChannelAttribute> attributes) {
                                String pkRoomId = getPkNameFromChannelAttr(attributes);
                                runOnUiThread(() -> {
                                    if (!TextUtils.isEmpty(pkRoomId)) {
                                        setupLocalVideo(roomId);
                                        setupRemoteVideo(pkRoomId);
                                    } else {
                                        setupLocalFullVideo(roomId);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(ErrorInfo errorInfo) {

                            }
                        });
                    }

                    @Override
                    public void onChannelAttributesUpdated(List<RtmChannelAttribute> list) {
                        String pkName = getPkNameFromChannelAttr(list);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(pkName)) {
                                    setupLocalVideo(roomId);
                                    setupRemoteVideo(pkName);
                                } else {
                                    removeRemoteVideo();
                                    setupLocalFullVideo(roomId);
                                }
                            }
                        });

                    }
                });
            }
        });
        rtmManager.login(UUID.randomUUID().toString(), null);
    }

    private void setupLocalFullVideo(String roomName){

        mDataBinding.flLocalFullContainer.setVisibility(View.VISIBLE);
        mDataBinding.llPkLayout.setVisibility(View.GONE);

        SurfaceView surfaceView = null;
        if(mDataBinding.flLocalContainer.getChildCount() > 0){
            surfaceView = (SurfaceView) mDataBinding.flLocalContainer.getChildAt(0);
        }

        // remove old video view
        mDataBinding.flLocalFullContainer.removeAllViews();
        mDataBinding.flLocalContainer.removeAllViews();

        if(agoraMediaPlayerKitA == null){
            agoraMediaPlayerKitA = playUrl(mDataBinding.flLocalFullContainer, getVideoPullUrl(roomName), () -> {
                mDataBinding.ivLoadingBg.setVisibility(View.GONE);
            });
        }else if(surfaceView != null){
            mDataBinding.flLocalFullContainer.addView(surfaceView);
        }else{
            throw new RuntimeException("setupLocalFullVideo failed");
        }
    }

    private void setupLocalVideo(String roomId) {

        mDataBinding.flLocalFullContainer.setVisibility(View.GONE);
        mDataBinding.llPkLayout.setVisibility(View.VISIBLE);

        SurfaceView surfaceView = null;
        if(mDataBinding.flLocalFullContainer.getChildCount() > 0){
            surfaceView = (SurfaceView) mDataBinding.flLocalFullContainer.getChildAt(0);
        }

        // remove old video view
        mDataBinding.flLocalFullContainer.removeAllViews();
        mDataBinding.flLocalContainer.removeAllViews();

        if(agoraMediaPlayerKitA == null){
            mDataBinding.ivLoadingBg.setVisibility(View.VISIBLE);
            agoraMediaPlayerKitA = playUrl(mDataBinding.flLocalContainer, getVideoPullUrl(roomId), () -> {
                mDataBinding.ivLoadingBg.setVisibility(View.GONE);
            });
        }else if(surfaceView != null){
            mDataBinding.flLocalContainer.addView(surfaceView);
        }else{
            throw new RuntimeException("setupLocalFullVideo failed");
        }
    }

    private void setupRemoteVideo(String pkRoomId) {
        removeRemoteVideo();
        mDataBinding.ivRemoteCover.setVisibility(View.VISIBLE);
        mDataBinding.ivRemoteCover.setImageResource(UserUtil.getUserProfileIcon(pkRoomId));
        agoraMediaPlayerKitB = playUrl(mDataBinding.flRemoteContainer, getVideoPullUrl(pkRoomId), () -> {
            mDataBinding.ivRemoteCover.setVisibility(View.GONE);
        });
    }

    private void removeRemoteVideo(){
        if(agoraMediaPlayerKitB != null){
            agoraMediaPlayerKitB.destroy();
            agoraMediaPlayerKitB = null;
            mDataBinding.flRemoteContainer.removeAllViews();
        }
    }


    private IMediaPlayer playUrl(FrameLayout container, String url, Runnable complete) {
        IMediaPlayer player = rtcManager.createPlayer();
        player.registerPlayerObserver(new IMediaPlayerObserver() {
            @Override
            public void onPlayerStateChanged(Constants.MediaPlayerState state, Constants.MediaPlayerError error) {
                Log.i(TAG, "agoraMediaPlayerKit1 onPlayerStateChanged:" + state + " " + error);
                if (state == Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                    player.play();
                    if(complete != null){
                        runOnUiThread(complete);
                    }
                }
            }

            @Override
            public void onPositionChanged(long l) {

            }

            @Override
            public void onPlayerEvent(Constants.MediaPlayerEvent mediaPlayerEvent, long l, String s) {

            }


            @Override
            public void onMetaData(Constants.MediaPlayerMetadataType mediaPlayerMetadataType, byte[] bytes) {

            }

            @Override
            public void onPlayBufferUpdated(long l) {

            }

            @Override
            public void onPreloadEvent(String s, Constants.MediaPlayerPreloadEvent mediaPlayerPreloadEvent) {

            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onAgoraCDNTokenWillExpire() {

            }

            @Override
            public void onPlayerSrcInfoChanged(io.agora.mediaplayer.data.SrcInfo srcInfo, io.agora.mediaplayer.data.SrcInfo srcInfo1) {

            }

            @Override
            public void onPlayerInfoUpdated(io.agora.mediaplayer.data.PlayerUpdatedInfo playerUpdatedInfo) {

            }

            @Override
            public void onAudioVolumeIndication(int i) {

            }

        });
        SurfaceView videoView = new SurfaceView(this);
        container.addView(videoView);
        player.setView(videoView);
        player.setRenderMode(PLAYER_RENDER_MODE_HIDDEN);
        player.open(url, 0);
        return player;
    }

    private String getPkNameFromChannelAttr(List<RtmChannelAttribute> attributes) {
        if (attributes.size() > 0) {
            RtmChannelAttribute pkAttribute = attributes.get(0);
            if (pkAttribute.getKey().equals("PK") && !TextUtils.isEmpty(pkAttribute.getValue())) {
                return pkAttribute.getValue();

            }
        }
        return "";
    }

    private String getAgoraAppId() {
        String appId = getString(R.string.agora_app_id);
        if (TextUtils.isEmpty(appId)) {
            throw new RuntimeException("the app id is empty");
        }
        return appId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (agoraMediaPlayerKitA != null) {
            agoraMediaPlayerKitA.destroy();
        }
        if (agoraMediaPlayerKitB != null) {
            agoraMediaPlayerKitB.destroy();
        }
        rtmManager.release();
        rtcManager.release();
    }

    private String getVideoPullUrl(String roomName) {
        return UrlManager.sUrl.getPullUrl(roomName);
    }
}
