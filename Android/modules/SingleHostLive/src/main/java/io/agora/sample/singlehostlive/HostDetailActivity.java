package io.agora.sample.singlehostlive;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.agora.sample.singlehostlive.databinding.HostDetailActivityBinding;
import io.agora.uiwidget.function.LiveRoomMessageListView;
import io.agora.uiwidget.utils.GiftUtil;
import io.agora.uiwidget.utils.RandomUtil;

public class  HostDetailActivity extends AppCompatActivity {

    private HostDetailActivityBinding mBinding;

    private final Handler mHandler = new Handler();
    private int userCount = 1;
    private LiveRoomMessageListView.LiveRoomMessageAdapter<RoomManager.MessageInfo> mMsgAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = HostDetailActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());

        mBinding.hostNameView.setName(RandomUtil.randomUserName(this));
        mBinding.hostNameView.setIcon(RandomUtil.randomLiveRoomIcon());

        mBinding.userView.setUserCount(userCount);
        mBinding.userView.addUserIcon(RandomUtil.randomLiveRoomIcon(), null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                userCount ++;
                mBinding.userView.setUserCount(userCount);
                mBinding.userView.addUserIcon(RandomUtil.randomLiveRoomIcon(), null);
                mHandler.postDelayed(this, 2000);
            }
        }, 2000);

        mBinding.bottomView.setFun1Visible(false);
        mBinding.bottomView.setFun2Visible(false);
        mBinding.bottomView.setupInputText(true, null);

        mMsgAdapter = new LiveRoomMessageListView.LiveRoomMessageAdapter<RoomManager.MessageInfo>() {
            @Override
            protected void onItemUpdate(LiveRoomMessageListView.MessageListViewHolder holder,
                                        RoomManager.MessageInfo item,
                                        int position) {
                holder.setupMessage(item.userName, item.content, item.giftIcon);
            }
        };
        mBinding.messageList.setAdapter(mMsgAdapter);
        mMsgAdapter.addMessage(new RoomManager.MessageInfo(
                RandomUtil.randomUserName(this),
                RandomUtil.randomUserName(this) + " " + getString(R.string.live_room_message_user_join_suffix)));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMsgAdapter.addMessage(new RoomManager.MessageInfo(
                        RandomUtil.randomUserName(HostDetailActivity.this),
                        RandomUtil.randomUserName(HostDetailActivity.this) + " " + getString(R.string.live_room_message_gift_prefix),
                        GiftUtil.getGiftIconRes(0)
                ));
                mHandler.postDelayed(this, 2000);
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
