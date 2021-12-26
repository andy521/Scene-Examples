package io.agora.sample.singlehostlive;

import android.os.Bundle;
import android.os.Handler;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import io.agora.sample.singlehostlive.databinding.HostDetailActivityBinding;
import io.agora.uiwidget.function.GiftGridDialog;
import io.agora.uiwidget.function.LiveBottomView;
import io.agora.uiwidget.function.LiveRoomMessageListView;
import io.agora.uiwidget.function.LiveToolsDialog;
import io.agora.uiwidget.function.VideoSettingDialog;
import io.agora.uiwidget.utils.GiftUtil;
import io.agora.uiwidget.utils.RandomUtil;

public class HostDetailActivity extends AppCompatActivity {

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
                userCount++;
                mBinding.userView.setUserCount(userCount);
                mBinding.userView.addUserIcon(RandomUtil.randomLiveRoomIcon(), null);
                mHandler.postDelayed(this, 2000);
            }
        }, 2000);

        mBinding.bottomView.setFun1Visible(true)
                .setFun1ImageResource(LiveBottomView.FUN_ICON_GIFT)
                .setFun1ClickListener(v -> showGiftGridDialog());
        mBinding.bottomView.setFun2Visible(false);
        mBinding.bottomView.setupInputText(true, null);
        mBinding.bottomView.setupCloseBtn(true, v -> finish());
        mBinding.bottomView.setupMoreBtn(true, v -> showSettingDialog());

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

    private void showGiftGridDialog() {
        new GiftGridDialog(HostDetailActivity.this)
                .setOnGiftSendClickListener((dialog, item, position) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void showSettingDialog() {
        new LiveToolsDialog(HostDetailActivity.this)
                .addToolItem(LiveToolsDialog.TOOL_ITEM_ROTATE, false, new LiveToolsDialog.OnItemClickListener() {
                    @Override
                    public void onItemClicked(View view, LiveToolsDialog.ToolItem item) {

                    }
                })
                .addToolItem(LiveToolsDialog.TOOL_ITEM_VIDEO, false, new LiveToolsDialog.OnItemClickListener() {
                    @Override
                    public void onItemClicked(View view, LiveToolsDialog.ToolItem item) {

                    }
                })
                .addToolItem(LiveToolsDialog.TOOL_ITEM_SETTING, false, new LiveToolsDialog.OnItemClickListener() {
                    @Override
                    public void onItemClicked(View view, LiveToolsDialog.ToolItem item) {
                        new VideoSettingDialog(HostDetailActivity.this)
                                .setResolutions(Arrays.asList(new Size(360, 640), new Size(720, 1080)))
                                .setFrameRates(Arrays.asList(10, 20, 30))
                                .setBitRateRange(0, 2000)
                                .setDefaultValues(new Size(360, 640), 10, 700)
                                .setOnValuesChangeListener(new VideoSettingDialog.OnValuesChangeListener() {
                                    @Override
                                    public void onResolutionChanged(Size resolution) {

                                    }

                                    @Override
                                    public void onFrameRateChanged(int framerate) {

                                    }

                                    @Override
                                    public void onBitrateChanged(int bitrate) {

                                    }
                                })
                                .show();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
