package io.agora.superapp.view;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import io.agora.superapp.Constants;
import io.agora.superapp.R;
import io.agora.superapp.databinding.ActivityPreviewBinding;
import io.agora.superapp.manager.RtcManager;
import io.agora.superapp.model.RoomInfo;
import io.agora.superapp.util.DataCallback;
import io.agora.superapp.util.RandomUtil;
import io.agora.superapp.util.UUIDUtil;
import io.agora.superapp.widget.WheelView;
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
        mDataBinding.livePrepareSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingDialog();
            }
        });
    }

    private void showSettingDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.live_room_dialog);
        View contentView = LayoutInflater.from(this).inflate(R.layout.action_room_settings, null);

        // 分辨率配置
        TextView resolutionTv = contentView.findViewById(R.id.live_room_setting_main_resolution_text);
        resolutionTv.setText(RtcManager.encoderConfiguration.dimensions.width + "x" + RtcManager.encoderConfiguration.dimensions.height);
        contentView.findViewById(R.id.live_room_setting_resolution).setOnClickListener(v -> {
            showResolutionDialog(data -> resolutionTv.setText(data.width + "x" + data.height));
        });

        // 帧率配置
        TextView framerateTv = contentView.findViewById(R.id.live_room_setting_main_framerate_text);
        framerateTv.setText(RtcManager.encoderConfiguration.frameRate + "");
        contentView.findViewById(R.id.live_room_setting_framerate).setOnClickListener(v -> {
            showFrameRateDialog(data -> framerateTv.setText(data + ""));
        });

        // 码率配置
        TextView bitrateTv = contentView.findViewById(R.id.live_room_setting_bitrate_value_text);
        bitrateTv.setText(RtcManager.encoderConfiguration.bitrate + "kbps");
        SeekBar seekBar = contentView.findViewById(R.id.live_room_setting_bitrate_progress_bar);
        seekBar.setMax(2000);
        seekBar.setProgress(RtcManager.encoderConfiguration.bitrate);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                RtcManager.encoderConfiguration.bitrate = progress;
                bitrateTv.setText(progress + "kbps");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        dialog.setContentView(contentView);
        dialog.setCanceledOnTouchOutside(true);
        hideStatusBar(dialog.getWindow(), false);
        dialog.show();
    }

    private void showResolutionDialog(DataCallback<VideoEncoderConfiguration.VideoDimensions> callback) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.live_room_dialog);
        View contentView = LayoutInflater.from(this).inflate(R.layout.action_room_settings_wheelview, null);

        WheelView wv = contentView.findViewById(R.id.wheel_view);
        wv.setOffset(2);
        List<String> items =  new ArrayList<>();
        int selectedIndex = 0;
        int index = 0;
        for (VideoEncoderConfiguration.VideoDimensions dimensions : RtcManager.RESOLUTIONS_PK_HOST) {
            items.add(String.format(Locale.US, "%d x %d", dimensions.width , dimensions.height));
            if (dimensions.width == RtcManager.encoderConfiguration.dimensions.width
                    && dimensions.height == RtcManager.encoderConfiguration.dimensions.height) {
                selectedIndex = index;
            }
            index ++;
        }
        wv.setItems(items);
        wv.setSeletion(selectedIndex);

        contentView.findViewById(R.id.live_room_setting_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoEncoderConfiguration.VideoDimensions dimensions = RtcManager.RESOLUTIONS_PK_HOST[wv.getSeletedIndex()];
                RtcManager.encoderConfiguration.dimensions = dimensions;
                if(callback != null){
                    callback.onSuccess(dimensions);
                }
                dialog.dismiss();
            }
        });
        contentView.findViewById(R.id.live_room_setting_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setContentView(contentView);
        dialog.setCanceledOnTouchOutside(true);
        hideStatusBar(dialog.getWindow(), false);
        dialog.show();

        disableDragging(dialog, contentView);
    }

    private void disableDragging(BottomSheetDialog dialog, View contentView) {
        if(!dialog.isShowing()){
            return;
        }
        BottomSheetBehavior behavior = BottomSheetBehavior.from((View) contentView.getParent());
        BottomSheetBehavior.BottomSheetCallback bottomSheetCallback
                = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet,
                                       @BottomSheetBehavior.State int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {//判断为向下拖动行为时，则强制设定状态为展开
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        };
        behavior.setBottomSheetCallback(bottomSheetCallback);
    }

    private void showFrameRateDialog(DataCallback<Integer> callback) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.live_room_dialog);
        View contentView = LayoutInflater.from(this).inflate(R.layout.action_room_settings_wheelview, null);

        WheelView wv = contentView.findViewById(R.id.wheel_view);
        wv.setOffset(2);
        List<String> items =  new ArrayList<>();
        int selectedIndex = 0;
        int index = 0;
        for (VideoEncoderConfiguration.FRAME_RATE rate : RtcManager.FRAME_RATES_PK_HOST) {
            items.add(String.format(Locale.US, "%d", rate.getValue()));
            if (rate.getValue() == RtcManager.encoderConfiguration.frameRate) {
                selectedIndex = index;
            }
            index ++;
        }
        wv.setItems(items);
        wv.setSeletion(selectedIndex);

        contentView.findViewById(R.id.live_room_setting_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = RtcManager.FRAME_RATES_PK_HOST[wv.getSeletedIndex()].getValue();
                RtcManager.encoderConfiguration.frameRate = value;
                if(callback != null){
                    callback.onSuccess(value);
                }
                dialog.dismiss();
            }
        });
        contentView.findViewById(R.id.live_room_setting_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setContentView(contentView);
        dialog.setCanceledOnTouchOutside(true);
        hideStatusBar(dialog.getWindow(), false);
        dialog.show();
        disableDragging(dialog, contentView);
    }

    private void hideStatusBar(Window window, boolean darkText) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && darkText) {
            flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        window.getDecorView().setSystemUiVisibility(flag |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
