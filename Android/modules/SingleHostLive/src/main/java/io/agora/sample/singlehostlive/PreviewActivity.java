package io.agora.sample.singlehostlive;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.Arrays;

import io.agora.uiwidget.function.PreviewControlView;
import io.agora.uiwidget.function.VideoSettingDialog;

public class PreviewActivity extends AppCompatActivity {
    private static final String TAG = "PreviewActivity";
    private RtcManager rtcManager = new RtcManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_activity);
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.CAMERA, Permission.Group.MICROPHONE)
                .onGranted(data -> initPreview())
                .start();


        PreviewControlView previewControlView = findViewById(R.id.preview_control_view);
        previewControlView.setBackIcon(true, v -> finish());
        previewControlView.setCameraIcon(true, v -> {
            rtcManager.switchCamera();
        });
        previewControlView.setBeautyIcon(false, null);
        previewControlView.setSettingIcon(true, v -> {
            // 视频参数设置弹窗
            new VideoSettingDialog(PreviewActivity.this)
                    .setResolutions(Arrays.asList(new Size(360, 640), new Size(720, 1080)))
                    .setFrameRates(Arrays.asList(10, 20, 30))
                    .setBitRateRange(0, 2000)
                    .setDefaultValues(new Size(360, 640), 10, 700)
                    .setOnValuesChangeListener(new VideoSettingDialog.OnValuesChangeListener() {
                        @Override
                        public void onResolutionChanged(Size resolution) {

                        }

                        @Override
                        public void onFrameRateChanged(int framerate) {

                        }

                        @Override
                        public void onBitrateChanged(int bitrate) {

                        }
                    })
                    .show();
        });
        previewControlView.setGoLiveBtn((view, randomName) -> {
            RoomManager.getInstance().createRoom(randomName, new RoomManager.DataCallback<RoomManager.RoomInfo>() {
                @Override
                public void onSuccess(RoomManager.RoomInfo data) {
                    Intent intent = new Intent(PreviewActivity.this, HostDetailActivity.class);
                    intent.putExtra("roomInfo", data);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailed(Exception e) {
                    Log.e(TAG, "", e);
                }
            });

        });
    }

    private void initPreview() {
        rtcManager.init(this, getString(R.string.agora_app_id), null);

        FrameLayout surfaceViewContainer = findViewById(R.id.surface_view_container);
        rtcManager.renderLocalVideo(surfaceViewContainer, null);

    }

    @Override
    public void finish() {
        rtcManager.release();
        super.finish();
    }

}
