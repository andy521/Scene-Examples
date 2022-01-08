package io.agora.sample.virtualimage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import io.agora.sample.virtualimage.databinding.VirtualImageHostDetailActivityBinding;
import io.agora.sample.virtualimage.manager.FUManager;
import io.agora.sample.virtualimage.manager.RoomManager;
import io.agora.sample.virtualimage.manager.RtcManager;
import io.agora.uiwidget.function.GiftAnimPlayDialog;
import io.agora.uiwidget.function.LiveRoomMessageListView;
import io.agora.uiwidget.function.LiveToolsDialog;
import io.agora.uiwidget.function.TextInputDialog;

public class HostDetailActivity extends AppCompatActivity {
    private final RtcManager rtcManager = RtcManager.getInstance();
    private final RoomManager roomManager = RoomManager.getInstance();
    private final FUManager fuManager = FUManager.getInstance();

    private VirtualImageHostDetailActivityBinding mBinding;
    private RoomManager.RoomInfo roomInfo;
    private LiveRoomMessageListView.LiveRoomMessageAdapter<RoomManager.MessageInfo> mMessageAdapter;
    private final RoomManager.DataCallback<RoomManager.GiftInfo> giftInfoDataCallback = new RoomManager.DataCallback<RoomManager.GiftInfo>() {
        @Override
        public void onSuccess(RoomManager.GiftInfo data) {
            runOnUiThread(() -> {
                mMessageAdapter.addMessage(new RoomManager.MessageInfo(
                        data.userId,
                        getString(R.string.live_room_message_gift_prefix),
                        data.getIconId()
                ));
                // 播放动画
                new GiftAnimPlayDialog(HostDetailActivity.this)
                        .setAnimRes(data.getGifId())
                        .show();
            });
        }

        @Override
        public void onFailed(Exception e) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = VirtualImageHostDetailActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());
        roomInfo = (RoomManager.RoomInfo) getIntent().getSerializableExtra("roomInfo");

        // 房间信息
        mBinding.hostNameView.setName(roomInfo.roomName);
        mBinding.hostNameView.setIcon(roomInfo.getAndroidBgId());

        // 底部按钮栏
        mBinding.bottomView.setFun1Visible(false);
        mBinding.bottomView.setFun2Visible(false);
        mBinding.bottomView.setupInputText(true, v -> {
            // 弹出文本输入框
            showTextInputDialog();
        });
        mBinding.bottomView.setupCloseBtn(true, v -> onBackPressed());
        mBinding.bottomView.setupMoreBtn(true, v -> showSettingDialog());

        // 消息列表
        mMessageAdapter = new LiveRoomMessageListView.LiveRoomMessageAdapter<RoomManager.MessageInfo>() {
            @Override
            protected void onItemUpdate(LiveRoomMessageListView.MessageListViewHolder holder, RoomManager.MessageInfo item, int position) {
                holder.setupMessage(item.userName, item.content, item.giftIcon);
            }
        };
        mBinding.messageList.setAdapter(mMessageAdapter);

        initFUManager();
        initRoomManager();
        initRtcManager();
    }

    private void showTextInputDialog() {
        new TextInputDialog(this)
                .setOnSendClickListener((v, text) -> mMessageAdapter.addMessage(new RoomManager.MessageInfo(RoomManager.getCacheUserId(), text)))
                .show();
    }

    private void initFUManager(){
        fuManager.initController();
        fuManager.setDriveMode();
        fuManager.showImage01();
    }

    private void initRoomManager() {
        roomManager.joinRoom(roomInfo.roomId, () -> roomManager.subscribeGiftReceiveEvent(roomInfo.roomId, new WeakReference<>(giftInfoDataCallback)));
    }

    private void initRtcManager() {
        rtcManager.renderLocalVideo(mBinding.fullVideoContainer, null);
        rtcManager.setVideoPreProcess(new RtcManager.VideoPreProcess() {
            @Override
            public RtcManager.ProcessVideoFrame processVideoFrameTex(byte[] img, int texId, float[] texMatrix, int width, int height, int cameraType) {
                FUManager.FuVideoFrame videoFrame = fuManager.processVideoFrame(img, texId, texMatrix, width, height, cameraType);
                if(videoFrame == null){
                    return null;
                }
                return new RtcManager.ProcessVideoFrame(videoFrame.texId, videoFrame.texMatrix, videoFrame.width, videoFrame.height,
                        videoFrame.texType == FUManager.TEXTURE_TYPE_OES ? RtcManager.TEXTURE_TYPE_OES : RtcManager.TEXTURE_TYPE_2D);
            }
        });
        rtcManager.joinChannel(roomInfo.roomId, roomInfo.userId, getString(R.string.virtual_image_agora_token), true, new RtcManager.OnChannelListener() {
            @Override
            public void onError(int code, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(HostDetailActivity.this, "code=" + code + ",message=" + message, Toast.LENGTH_LONG).show();
                    finish();
                });

            }

            @Override
            public void onJoinSuccess(int uid) {
                runOnUiThread(() -> mMessageAdapter.addMessage(new RoomManager.MessageInfo(uid + "", getString(R.string.live_room_message_user_join_suffix))));

            }

            @Override
            public void onUserJoined(String channelId, int uid) {
                runOnUiThread(() -> {
                    mMessageAdapter.addMessage(new RoomManager.MessageInfo(uid + "", getString(R.string.live_room_message_user_join_suffix)));
                    mBinding.remoteVideoControl.setVisibility(View.VISIBLE);
                    mBinding.remoteVideoControl.setCloseVisible(false);
                    mBinding.remoteVideoControl.setName(uid + "");
                    rtcManager.renderRemoteVideo(mBinding.remoteVideoControl.getVideoContainer(), uid);
                });
            }

            @Override
            public void onUserOffline(String channelId, int uid) {
                runOnUiThread(() -> {
                    mMessageAdapter.addMessage(new RoomManager.MessageInfo(uid + "", getString(R.string.live_room_message_user_left_suffix)));
                    mBinding.remoteVideoControl.setVisibility(View.GONE);
                });
            }
        });
    }

    private void showSettingDialog() {
        new LiveToolsDialog(HostDetailActivity.this)
                .addToolItem(LiveToolsDialog.TOOL_ITEM_ROTATE, false, (view, item) -> rtcManager.switchCamera())
                .show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.common_tip)
                .setMessage(R.string.common_tip_close_room)
                .setPositiveButton(R.string.common_yes, (dialog, which) -> {
                    dialog.dismiss();
                    roomManager.destroyRoom(roomInfo.roomId);
                    rtcManager.reset(true);
                    fuManager.reset();
                    HostDetailActivity.super.onBackPressed();
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }


}
