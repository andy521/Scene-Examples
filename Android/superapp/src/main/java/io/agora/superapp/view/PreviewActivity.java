package io.agora.superapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Objects;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.superapp.Constants;
import io.agora.superapp.R;
import io.agora.superapp.databinding.ActivityPreviewBinding;
import io.agora.superapp.manager.RtcManager;
import io.agora.superapp.model.RoomInfo;
import io.agora.superapp.util.RandomUtil;
import io.agora.superapp.util.UUIDUtil;
import io.agora.syncmanager.rtm.IObject;
import io.agora.syncmanager.rtm.Scene;
import io.agora.syncmanager.rtm.SyncManager;
import io.agora.syncmanager.rtm.SyncManagerException;

public class PreviewActivity extends DataBindBaseActivity<ActivityPreviewBinding> {

    private RtcManager rtcManager;

    @Override
    protected void iniBundle(@NonNull Bundle bundle) {
        // do nothing
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_preview;
    }

    @Override
    protected void iniView() {
        mDataBinding.roomNameEdit.setText(RandomUtil.randomLiveRoomName(this));
    }

    @Override
    protected void iniListener() {
        mDataBinding.randomBtn.setOnClickListener(v -> {
            mDataBinding.roomNameEdit.setText(RandomUtil.randomLiveRoomName(this));
        });
        mDataBinding.livePreparePolicyClose.setOnClickListener(v -> {
            mDataBinding.livePreparePolicyCautionLayout.setVisibility(View.GONE);
        });
        mDataBinding.livePrepareSwitchCamera.setOnClickListener(v -> rtcManager.switchCamera());
        mDataBinding.livePrepareClose.setOnClickListener(v -> finish());
        mDataBinding.livePrepareModeChoice.setOnCheckedChangeListener((group, checkedId) -> {
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
        mDataBinding.livePrepareModeChoice.check(R.id.rb_mode_direct_cdn);
        mDataBinding.livePrepareGoLiveBtn.setOnClickListener(v -> {
            long currTime = System.currentTimeMillis();
            int mode = mDataBinding.livePrepareModeChoice.getCheckedRadioButtonId() == R.id.rb_mode_direct_cdn ? RoomInfo.PUSH_MODE_DIRECT_CDN : RoomInfo.PUSH_MODE_RTC;
            createRoom(new RoomInfo(currTime, currTime + "", Objects.requireNonNull(mDataBinding.roomNameEdit.getText()).toString(), mode));
        });
    }

    @Override
    protected void iniData() {
        rtcManager = new RtcManager();
        rtcManager.init(this, getString(R.string.agora_app_id), null);
        rtcManager.renderLocalVideo(mDataBinding.localPreviewLayout);
    }

    @Override
    public void finish() {
        rtcManager.release();
        super.finish();
    }

    private void createRoom(RoomInfo roomInfo){
        Scene room = new Scene();
        room.setId(roomInfo.roomId);
        room.setUserId(UUIDUtil.getUUID());
        room.setProperty(roomInfo.toMap());
        SyncManager.Instance().createScene(room, new SyncManager.Callback() {
            @Override
            public void onSuccess() {
                SyncManager.Instance()
                        .getScene(roomInfo.roomId)
                        .collection(Constants.SYNC_COLLECTION_ROOM_INFO)
                        .add(roomInfo.toMap(), new SyncManager.DataItemCallback() {
                            @Override
                            public void onSuccess(IObject result) {
                                rtcManager.release();
                                runOnUiThread(() -> {
                                    roomInfo.objectId = result.getId();
                                    startActivity(HostActivity.launch(PreviewActivity.this, roomInfo));
                                    finish();
                                });
                            }

                            @Override
                            public void onFail(SyncManagerException exception) {
                                runOnUiThread(() -> Toast.makeText(PreviewActivity.this, "Room create failed -- " + exception.toString(), Toast.LENGTH_SHORT).show());

                                SyncManager.Instance()
                                        .getScene(roomInfo.roomId).delete(new SyncManager.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        // do nothing
                                    }

                                    @Override
                                    public void onFail(SyncManagerException exception) {
                                        runOnUiThread(() -> Toast.makeText(PreviewActivity.this, "Room create failed -- " + exception.toString(), Toast.LENGTH_SHORT).show());
                                    }
                                });
                            }
                        });
            }

            @Override
            public void onFail(SyncManagerException exception) {
                runOnUiThread(() -> Toast.makeText(PreviewActivity.this, "Room create failed -- " + exception.toString(), Toast.LENGTH_SHORT).show());
            }
        });
    }

}
