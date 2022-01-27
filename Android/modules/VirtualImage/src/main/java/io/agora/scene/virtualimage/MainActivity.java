package io.agora.scene.virtualimage;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;

import io.agora.example.base.BaseActivity;
import io.agora.scene.virtualimage.databinding.VirtualImageActivityMainBinding;
import io.agora.scene.virtualimage.manager.FUManager;
import io.agora.scene.virtualimage.util.OneUtil;

public class MainActivity extends BaseActivity<VirtualImageActivityMainBinding> {

    private GlobalViewModel globalViewModel;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        globalViewModel = OneUtil.getViewModel(this, GlobalViewModel.class);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        FUManager.getInstance().handleTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (globalViewModel != null)
            globalViewModel.focused.setValue(hasFocus);
    }
}