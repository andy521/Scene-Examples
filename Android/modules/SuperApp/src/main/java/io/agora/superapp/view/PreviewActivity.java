package io.agora.superapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.UUID;

import io.agora.example.base.BaseActivity;
import io.agora.superapp.R;
import io.agora.superapp.databinding.ActivityPreviewBinding;
import io.agora.superapp.manager.RtcManager;
import io.agora.superapp.model.RoomInfo;
import io.agora.superapp.util.RandomUtil;
import io.agora.syncmanager.rtm.Scene;
import io.agora.syncmanager.rtm.Sync;
import io.agora.syncmanager.rtm.SyncManagerException;

public class PreviewActivity extends BaseActivity<ActivityPreviewBinding> {

    private RtcManager rtcManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding.roomNameEdit.setText(RandomUtil.randomLiveRoomName(this));
        mBinding.randomBtn.setOnClickListener(v -> {
            mBinding.roomNameEdit.setText(RandomUtil.randomLiveRoomName(this));
        });
        mBinding.livePreparePolicyClose.setOnClickListener(v -> {
            mBinding.livePreparePolicyCautionLayout.setVisibility(View.GONE);
        });
        mBinding.livePrepareSwitchCamera.setOnClickListener(v -> rtcManager.switchCamera());
        mBinding.livePrepareClose.setOnClickListener(v -> finish());
        mBinding.livePrepareModeChoice.setOnCheckedChangeListener((group, checkedId) -> {
            int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                RadioButton childAt = (RadioButton) group.getChildAt(i);
                if(childAt.getId() == checkedId){
                    childAt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_baseline_check_circle_outline_24);
                }else{
                    childAt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        });
        mBinding.livePrepareModeChoice.check(R.id.rb_mode_direct_cdn);
        mBinding.livePrepareGoLiveBtn.setOnClickListener(v -> {
            long currTime = System.currentTimeMillis();
            int mode = mBinding.livePrepareModeChoice.getCheckedRadioButtonId() == R.id.rb_mode_direct_cdn ? RoomInfo.PUSH_MODE_DIRECT_CDN : RoomInfo.PUSH_MODE_RTC;
            createRoom(new RoomInfo(currTime, currTime + "", Objects.requireNonNull(mBinding.roomNameEdit.getText()).toString(), mode));
        });

        rtcManager = new RtcManager();
        rtcManager.init(this, getString(R.string.agora_app_id), null);
        rtcManager.renderLocalVideo(mBinding.localPreviewLayout, null);
    }


    @Override
    public void finish() {
        rtcManager.release();
        super.finish();
    }

    private void createRoom(RoomInfo roomInfo){
        Scene room = new Scene();
        room.setId(roomInfo.roomId);
        room.setUserId(UUID.randomUUID().toString());
        room.setProperty(roomInfo.toMap());
        Sync.Instance().createScene(room, new Sync.Callback() {
            @Override
            public void onSuccess() {
                rtcManager.release();
                startActivity(HostActivity.launch(PreviewActivity.this, roomInfo));
                finish();
            }

            @Override
            public void onFail(SyncManagerException exception) {
                Toast.makeText(PreviewActivity.this, "Room create failed -- " + exception.toString(), Toast.LENGTH_SHORT).show();

            }
        });
    }

}
