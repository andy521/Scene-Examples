package io.agora.ktv.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.agora.data.manager.UserManager;
import com.agora.data.model.AgoraMember;
import com.agora.data.model.AgoraRoom;
import com.agora.data.model.User;
import com.agora.data.sync.RoomManager;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.baselibrary.util.ToastUtile;
import io.agora.ktv.R;
import io.agora.ktv.databinding.KtvActivityRoomBinding;
import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 * 房间
 *
 * @author chenhengfei@agora.io
 */
public class RoomActivity extends DataBindBaseActivity<KtvActivityRoomBinding> implements View.OnClickListener {
    private static final String TAG_ROOM = "room";

    public static Intent newIntent(Context context, AgoraRoom mRoom) {
        Intent intent = new Intent(context, RoomActivity.class);
        intent.putExtra(TAG_ROOM, mRoom);
        return intent;
    }

    @Override
    protected void iniBundle(@NonNull Bundle bundle) {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ktv_activity_room;
    }

    @Override
    protected void iniView() {
    }

    @Override
    protected void iniListener() {
        mDataBinding.btMic.setOnClickListener(this);
        mDataBinding.btRequest.setOnClickListener(this);
    }

    private AgoraMember mAgoraMember;

    @Override
    protected void iniData() {
        User mUser = UserManager.Instance(this).getUserLiveData().getValue();
        if (mUser == null) {
            ToastUtile.toastShort(this, "please login in");
            finish();
            return;
        }

        AgoraRoom mRoom = (AgoraRoom) getIntent().getExtras().getSerializable(TAG_ROOM);

        RoomManager.Instance(this)
                .joinRoom(mRoom, mUser)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<AgoraMember>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull AgoraMember agoraMember) {
                        mAgoraMember = agoraMember;
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == mDataBinding.btMic) {
            toggleMic();
        } else if (v == mDataBinding.btRequest) {
            changeRole();
        }
    }

    private void toggleMic() {
        AgoraRoom mRoom = (AgoraRoom) getIntent().getExtras().getSerializable(TAG_ROOM);
        RoomManager.Instance(this)
                .toggleSelfAudio(mRoom, mAgoraMember, true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    private void changeRole() {

    }
}
