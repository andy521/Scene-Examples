package io.agora.scene.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.agora.data.provider.AgoraObject;
import com.agora.data.provider.CollectionReference;
import com.agora.data.provider.DocumentReference;
import com.agora.data.provider.SyncManager;

import java.util.List;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.scene.R;
import io.agora.scene.databinding.ActivitySplashBinding;

/**
 * 闪屏界面
 *
 * @author chenhengfei@agora.io
 */
public class SplashActivity extends DataBindBaseActivity<ActivitySplashBinding> implements View.OnClickListener {

    @Override
    protected void iniBundle(@NonNull Bundle bundle) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void iniView() {

    }

    @Override
    protected void iniListener() {
        SyncManager.Instance()
                .getRoom("123456")
                .get(new DocumentReference.DataCallback() {
                    @Override
                    public void onSuccess(AgoraObject result) {

                    }

                    @Override
                    public void onFail(int code, String msg) {

                    }
                });

        SyncManager.Instance()
                .getRoom("123456")
                .delete(new DocumentReference.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFail(int code, String msg) {

                    }
                });

        SyncManager.Instance()
                .getRoom("123456")
                .update("user", "1", new DocumentReference.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFail(int code, String msg) {

                    }
                });

        SyncManager.Instance()
                .getRoom("123456")
                .collection("MEMBERS")
                .get(new CollectionReference.DataCallback() {
                    @Override
                    public void onSuccess(List<AgoraObject> result) {

                    }

                    @Override
                    public void onFail(int code, String msg) {

                    }
                });

        SyncManager.Instance()
                .getRoom("123456")
                .collection("MEMBERS")
                .add(new Object());

        SyncManager.Instance()
                .getRoom("123456")
                .collection("MEMBERS")
                .document("MEMBERID")
                .get(new DocumentReference.DataCallback() {

                    @Override
                    public void onSuccess(AgoraObject result) {

                    }

                    @Override
                    public void onFail(int code, String msg) {

                    }
                });

        SyncManager.Instance()
                .getRoom("123456")
                .whereEQ("isW", true)
                .subcribe(new SyncManager.EventListener() {
                    @Override
                    public void onCreated(AgoraObject item) {

                    }

                    @Override
                    public void onUpdated(AgoraObject item) {

                    }

                    @Override
                    public void onDeleted(String objectId) {

                    }

                    @Override
                    public void onSubscribeError(int error) {

                    }
                });
//        mDataBinding.tvLivecast.setOnClickListener(this);
//        mDataBinding.tvMerry.setOnClickListener(this);
    }

    @Override
    protected void iniData() {
    }

    @Override
    public void onClick(View v) {
        if (v == mDataBinding.tvLivecast) {
            Intent intent = new Intent(this, io.agora.interactivepodcast.activity.RoomListActivity.class);
            startActivity(intent);
        } else if (v == mDataBinding.tvMerry) {
            Intent intent = new Intent(this, io.agora.marriageinterview.activity.RoomListActivity.class);
            startActivity(intent);
        }
    }
}
