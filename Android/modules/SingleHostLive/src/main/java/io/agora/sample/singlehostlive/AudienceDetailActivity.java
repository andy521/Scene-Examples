package io.agora.sample.singlehostlive;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.agora.sample.singlehostlive.databinding.AudienceDetailActivityBinding;
import io.agora.uiwidget.utils.RandomUtil;

public class AudienceDetailActivity extends AppCompatActivity {

    private RtcManager rtcManager = new RtcManager();
    private AudienceDetailActivityBinding mBinding;
    private RoomManager.RoomInfo roomInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = AudienceDetailActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());
        roomInfo = (RoomManager.RoomInfo) getIntent().getSerializableExtra("roomInfo");
        rtcManager.init(this, getString(R.string.agora_app_id), null);

        mBinding.hostNameView.setName(RandomUtil.randomUserName(this));
        mBinding.hostNameView.setIcon(roomInfo.getAndroidBgId());

        mBinding.bottomView.setFun1Visible(false);
        mBinding.bottomView.setFun2Visible(false);
        mBinding.bottomView.setupInputText(false, null);
        mBinding.bottomView.setupCloseBtn(true, v -> finish());
        mBinding.bottomView.setupMoreBtn(false, null);

        rtcManager.joinChannel(roomInfo.roomId, roomInfo.userId, false, new RtcManager.OnChannelListener() {
            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onJoinSuccess(int uid) {

            }

            @Override
            public void onUserJoined(String channelId, int uid) {
                runOnUiThread(() -> rtcManager.renderRemoteVideo(mBinding.fullVideoContainer, channelId, uid));
            }
        });

    }

    @Override
    public void finish() {
        rtcManager.release();
        super.finish();
    }
}
