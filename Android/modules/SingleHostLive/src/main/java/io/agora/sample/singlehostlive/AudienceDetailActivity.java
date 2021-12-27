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
        rtcManager.init(this, getString(R.string.agora_app_id), new RtcManager.OnInitializeListener() {
            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onUserJoined(int uid) {
                runOnUiThread(() -> rtcManager.renderRemoteVideo(mBinding.fullVideoContainer, uid, null));
            }

            @Override
            public void onUserLeaved(int uid, int reason) {

            }
        });

        mBinding.hostNameView.setName(RandomUtil.randomUserName(this));
        mBinding.hostNameView.setIcon(roomInfo.getAndroidBgId());

        mBinding.bottomView.setFun1Visible(false);
        mBinding.bottomView.setFun2Visible(false);
        mBinding.bottomView.setupInputText(false, null);
        mBinding.bottomView.setupCloseBtn(true, v -> finish());
        mBinding.bottomView.setupMoreBtn(false, null);

        rtcManager.joinChannel(roomInfo.roomId, RandomUtil.randomId() + "", false);
    }

    @Override
    public void finish() {
        rtcManager.release();
        super.finish();
    }
}
