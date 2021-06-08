package io.agora.ktv.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.agora.data.model.AgoraMember;
import com.agora.data.model.AgoraRoom;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.ktv.R;
import io.agora.ktv.databinding.KtvActivityRoomBinding;


/**
 * 创建房间
 *
 * @author chenhengfei@agora.io
 */
public class CreateRoomActivity extends DataBindBaseActivity<KtvActivityRoomBinding> implements View.OnClickListener {
    private static final String TAG_ROOM = "room";

    public static Intent newIntent(Context context, AgoraRoom mRoom) {
        Intent intent = new Intent(context, CreateRoomActivity.class);
        intent.putExtra(TAG_ROOM, mRoom);
        return intent;
    }

    @Override
    protected void iniBundle(@NonNull Bundle bundle) {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ktv_activity_create_room;
    }

    @Override
    protected void iniView() {
    }

    @Override
    protected void iniListener() {
    }

    private AgoraMember mAgoraMember;

    @Override
    protected void iniData() {

    }

    @Override
    public void onClick(View v) {
//        if (v == mDataBinding.btMic) {
//            toggleMic();
//        } else if (v == mDataBinding.btRequest) {
//            changeRole();
//        }
    }
}
