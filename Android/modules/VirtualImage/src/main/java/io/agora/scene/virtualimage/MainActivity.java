package io.agora.scene.virtualimage;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;

import io.agora.example.base.BaseActivity;
import io.agora.rtc2.RtcEngine;
import io.agora.scene.virtualimage.databinding.VirtualImageActivityMainBinding;
import io.agora.scene.virtualimage.manager.FUManager;
import io.agora.scene.virtualimage.manager.RtcManager;
import io.agora.scene.virtualimage.util.OneUtil;
import io.agora.syncmanager.rtm.Sync;

public class MainActivity extends BaseActivity<VirtualImageActivityMainBinding> {

    private GlobalViewModel globalViewModel;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        globalViewModel = OneUtil.getViewModel(this, GlobalViewModel.class);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        RtcManager.getInstance().init(this, getString(R.string.rtc_app_id), null);
        FUManager.getInstance().init(this);
    }

    @Override
    public void finish() {
        super.finish();

        new Thread(() -> {
//        RTMDestroy
            Sync.Instance().destroy();
//        RTCDestroy
            RtcEngine.destroy();
        }).start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (globalViewModel != null)
            globalViewModel.focused.setValue(hasFocus);
    }
}