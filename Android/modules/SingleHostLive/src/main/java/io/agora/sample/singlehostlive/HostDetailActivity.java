package io.agora.sample.singlehostlive;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.agora.sample.singlehostlive.databinding.HostDetailActivityBinding;
import io.agora.uiwidget.function.LiveToolsDialog;

public class HostDetailActivity extends AppCompatActivity {
    private static final String TAG = "HostDetailActivity";
    private RtcManager rtcManager = new RtcManager();
    private HostDetailActivityBinding mBinding;
    private RoomManager.RoomInfo roomInfo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = HostDetailActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());
        roomInfo = (RoomManager.RoomInfo) getIntent().getSerializableExtra("roomInfo");
        Log.d(TAG, "roomInfo=" + roomInfo.toString());
        rtcManager.init(this, getString(R.string.agora_app_id), new RtcManager.OnInitializeListener() {
            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onUserJoined(int uid) {

            }

            @Override
            public void onUserLeaved(int uid, int reason) {

            }
        });

        mBinding.hostNameView.setName(roomInfo.roomName);
        mBinding.hostNameView.setIcon(roomInfo.getAndroidBgId());

        mBinding.bottomView.setFun1Visible(false);
        mBinding.bottomView.setFun2Visible(false);
        mBinding.bottomView.setupInputText(false, null);
        mBinding.bottomView.setupCloseBtn(true, v -> finish());
        mBinding.bottomView.setupMoreBtn(true, v -> showSettingDialog());

        rtcManager.renderLocalVideo(mBinding.fullVideoContainer, null);
        rtcManager.joinChannel(roomInfo.roomId, roomInfo.userId, true);
    }

    private void showSettingDialog() {
        new LiveToolsDialog(HostDetailActivity.this)
                .addToolItem(LiveToolsDialog.TOOL_ITEM_ROTATE, false, (view, item) -> rtcManager.switchCamera())
                .show();
    }

    @Override
    public void finish() {
        rtcManager.release();
        super.finish();
    }

}
