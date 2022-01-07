package io.agora.sample.virtualimage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc2.video.VideoEncoderConfiguration;
import io.agora.sample.virtualimage.manager.FUManager;
import io.agora.sample.virtualimage.manager.RoomManager;
import io.agora.sample.virtualimage.manager.RtcManager;
import io.agora.uiwidget.function.PreviewControlView;
import io.agora.uiwidget.function.VideoSettingDialog;

public class PreviewActivity extends AppCompatActivity {
    private static final String TAG = "PreviewActivity";
    private final RtcManager rtcManager = RtcManager.getInstance();
    private final FUManager fuManager = FUManager.getInstance();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.virtual_image_preview_activity);
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
            List<Size> resolutions = new ArrayList<>();
            for (VideoEncoderConfiguration.VideoDimensions sVideoDimension : RtcManager.sVideoDimensions) {
                resolutions.add(new Size(sVideoDimension.width, sVideoDimension.height));
            }
            List<Integer> frameRates = new ArrayList<>();
            for (VideoEncoderConfiguration.FRAME_RATE sFrameRate : RtcManager.sFrameRates) {
                frameRates.add(sFrameRate.getValue());
            }
            new VideoSettingDialog(PreviewActivity.this)
                    .setResolutions(resolutions)
                    .setFrameRates(frameRates)
                    .setBitRateRange(0, 2000)
                    .setDefaultValues(new Size(RtcManager.encoderConfiguration.dimensions.width, RtcManager.encoderConfiguration.dimensions.height),
                            RtcManager.encoderConfiguration.frameRate, RtcManager.encoderConfiguration.bitrate)
                    .setOnValuesChangeListener(new VideoSettingDialog.OnValuesChangeListener() {
                        @Override
                        public void onResolutionChanged(Size resolution) {
                            RtcManager.encoderConfiguration.dimensions = new VideoEncoderConfiguration.VideoDimensions(resolution.getWidth(), resolution.getHeight());
                        }

                        @Override
                        public void onFrameRateChanged(int framerate) {
                            RtcManager.encoderConfiguration.frameRate = framerate;
                        }

                        @Override
                        public void onBitrateChanged(int bitrate) {
                            RtcManager.encoderConfiguration.bitrate = bitrate;
                        }
                    })
                    .show();
        });
        previewControlView.setGoLiveBtn((view, randomName) -> {
            RoomManager.getInstance().createRoom(randomName, new RoomManager.DataCallback<RoomManager.RoomInfo>() {
                @Override
                public void onSuccess(RoomManager.RoomInfo data) {
                    rtcManager.reset(false);
                    fuManager.reset();
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

        initFUManager();
    }

    private void initFUManager(){
        fuManager.initController();
        fuManager.enterBodyDriveMode();
        fuManager.showImage01();
    }

    private void initPreview() {
        rtcManager.init(this, getString(R.string.virtual_image_agora_app_id), null);
        rtcManager.setVideoPreProcess(new RtcManager.VideoPreProcess() {
            @Override
            public RtcManager.ProcessVideoFrame processVideoFrameTex(byte[] img, int texId, float[] texMatrix, int width, int height, int cameraType) {
                FUManager.FuVideoFrame videoFrame = fuManager.processVideoFrame(img, texId,texMatrix, width, height, cameraType);
                return new RtcManager.ProcessVideoFrame(videoFrame.texId, videoFrame.texMatrix, videoFrame.width, videoFrame.height,
                        videoFrame.texType == FUManager.TEXTURE_TYPE_OES ? RtcManager.TEXTURE_TYPE_OES : RtcManager.TEXTURE_TYPE_2D);
            }
        });

        FrameLayout surfaceViewContainer = findViewById(R.id.surface_view_container);
        rtcManager.renderLocalVideo(surfaceViewContainer, null);
    }

    @Override
    public void onBackPressed() {
        rtcManager.reset(true);
        fuManager.reset();
        super.onBackPressed();
    }
}
