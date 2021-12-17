package io.agora.sample.rtegame;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;

import io.agora.example.base.BaseActivity;
import io.agora.example.base.BaseUtil;
import io.agora.rtc2.RtcEngine;
import io.agora.sample.rtegame.databinding.ActivityMainBinding;
import io.agora.sample.rtegame.util.GameUtil;
import io.agora.syncmanager.rtm.Sync;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

    private GlobalViewModel globalViewModel;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        globalViewModel = GameUtil.getViewModel(this, GlobalViewModel.class);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setBackgroundDrawable(null);
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