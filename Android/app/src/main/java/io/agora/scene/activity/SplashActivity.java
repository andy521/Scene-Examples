package io.agora.scene.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.example.base.BaseActivity;
import io.agora.livepk.activity.LivePKListActivity;
import io.agora.scene.R;
import io.agora.scene.databinding.ActivitySplashBinding;

/**
 * 闪屏界面
 *
 * @author chenhengfei@agora.io
 */
public class SplashActivity extends BaseActivity<ActivitySplashBinding> implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.tvLivePK.setOnClickListener(this);
        mBinding.tvBreakoutRoom.setOnClickListener(this);
        mBinding.tvLive.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v == mBinding.tvLivePK) {
            Intent intent = new Intent(this, LivePKListActivity.class);
            startActivity(intent);
        } else if (v == mBinding.tvBreakoutRoom) {
            Intent intent = new Intent(this, io.agora.sample.breakoutroom.ui.MainActivity.class);
            startActivity(intent);
        } else if (v == mBinding.tvLive) {
            Intent intent = new Intent(this, io.agora.sample.live.RoomListActivity.class);
            startActivity(intent);
        }
    }
}
