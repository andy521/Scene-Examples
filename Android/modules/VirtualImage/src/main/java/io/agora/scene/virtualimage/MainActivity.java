package io.agora.scene.virtualimage;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
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
        setStatusBarTransparent();
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

    private void setStatusBarTransparent() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}