package io.agora.sample.singlehostlive;

import android.content.Intent;
import android.os.Bundle;
import android.util.Size;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.Arrays;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.uiwidget.function.PreviewControlView;
import io.agora.uiwidget.function.VideoSettingDialog;

public class PreviewActivity extends AppCompatActivity {

    private RtcEngine rtcEngine;

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
            if(rtcEngine != null){
                rtcEngine.switchCamera();
            }
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
            startActivity(new Intent(PreviewActivity.this, HostDetailActivity.class));
            finish();
        });
    }

    private void initPreview() {
        try {
            rtcEngine = RtcEngine.create(this, getString(R.string.agora_app_id), new IRtcEngineEventHandler() {
                @Override
                public void onError(int err) {
                    super.onError(err);
                }
            });
            rtcEngine.enableVideo();
            FrameLayout surfaceViewContainer = findViewById(R.id.surface_view_container);
            SurfaceView videoView = RtcEngine.CreateRendererView(this);
            surfaceViewContainer.addView(videoView);
            rtcEngine.setupLocalVideo(new VideoCanvas(videoView));
            rtcEngine.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        rtcEngine.stopPreview();
        RtcEngine.destroy();
        super.finish();
    }

}
