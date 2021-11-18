package io.agora.superapp.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.superapp.Constants;
import io.agora.superapp.R;
import io.agora.superapp.databinding.ActivityVideoBinding;
import io.agora.superapp.manager.RtcManager;
import io.agora.superapp.model.RoomInfo;
import io.agora.superapp.model.UserInfo;
import io.agora.superapp.util.DataCallback;
import io.agora.superapp.util.UserUtil;
import io.agora.syncmanager.rtm.IObject;
import io.agora.syncmanager.rtm.SyncManager;
import io.agora.syncmanager.rtm.SyncManagerException;

import static io.agora.superapp.Constants.SYNC_COLLECTION_ROOM_INFO;
import static io.agora.superapp.Constants.SYNC_COLLECTION_USER_INFO;
import static io.agora.superapp.Constants.SYNC_DEFAULT_CHANNEL;

public class HostActivity extends DataBindBaseActivity<ActivityVideoBinding> {
    private static final String TAG = "HostPKActivity";

    private static final String EXTRA_ROOM_INFO = "roomInfo";

    public static Intent launch(Context context, RoomInfo roomInfo) {
        Intent intent = new Intent(context, HostActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        return intent;
    }

    private final RtcManager rtcManager = new RtcManager();
    private RoomInfo mRoomInfo;

    @Override
    protected void iniBundle(@NonNull Bundle bundle) {
        mRoomInfo = ((RoomInfo) getIntent().getSerializableExtra(EXTRA_ROOM_INFO));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video;
    }

    @Override
    protected void iniView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setStatusBarStyle(true);
        setTitle(mRoomInfo.roomName);

        int userProfileIcon = UserUtil.getUserProfileIcon(getLocalRoomId());

        mDataBinding.ivLoadingBg.setVisibility(View.VISIBLE);
        mDataBinding.ivLoadingBg.setImageResource(userProfileIcon);

        Glide.with(this)
                .load(userProfileIcon)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(mDataBinding.ivRoomAvatar);

        mDataBinding.tvRoomName.setText(String.format(Locale.US, "%s(%s)", mRoomInfo.roomName, mRoomInfo.roomId));
        updateRoomUserCountTv();

        mDataBinding.liveBottomBtnMore.setOnClickListener(v -> {
            showMoreChoiceDialog();
        });

    }

    private void updateRoomUserCountTv() {
        mDataBinding.liveParticipantCountText.setText(mRoomInfo.userCount + "");
    }

    @Override
    protected void iniListener() {
        mDataBinding.liveBottomBtnClose.setOnClickListener(v -> onBackPressed());
        mDataBinding.llParticipant.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mRoomInfo.userIdPK)) {
                loadRoomUserInfoList();
            } else {
                Toast.makeText(this, "The current room is linking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void iniData() {
        initSyncManager();

        initRTCManager();
        setupLocalFullView();
        startStreaming();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        checkIsPKing(() -> cleanCurrRoomInfo(this::finish),
                data -> showPKEndDialog());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcManager.release();
    }

    //============================UI Logic===============================================

    private void showMoreChoiceDialog() {
        final int itemPadding = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        final int gridSpan = 4;
        final int[] toolIcons = {
                R.drawable.icon_rotate,
                R.drawable.action_sheet_tool_speaker
        };
        final String[] toolNames = getResources().getStringArray(R.array.live_room_action_sheet_tool_names);

        final class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView iconIv;
            final TextView toolNameTv;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                iconIv = itemView.findViewById(R.id.live_room_action_sheet_tool_item_icon);
                toolNameTv = itemView.findViewById(R.id.live_room_action_sheet_tool_item_name);
            }
        }

        RelativeLayout dialogView = new RelativeLayout(this);
        LayoutInflater.from(this).inflate(R.layout.action_tool, dialogView, true);
        RecyclerView recyclerView = dialogView.findViewById(R.id.live_room_action_sheet_tool_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, gridSpan));
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.action_tool_item, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.toolNameTv.setText(toolNames[position]);
                holder.iconIv.setImageResource(toolIcons[position]);
                switch (position) {
                    case 1:
                        // 静音
                        holder.iconIv.setActivated(!RtcManager.isMuteLocalAudio);
                        break;
                }

                holder.iconIv.setOnClickListener(v -> {
                    switch (position) {
                        case 0:
                            // 翻转摄像头
                            rtcManager.switchCamera();
                            break;
                        case 1:
                            // 静音
                            rtcManager.muteLocalAudio(!RtcManager.isMuteLocalAudio);
                            holder.iconIv.setActivated(!RtcManager.isMuteLocalAudio);
                            break;
                    }
                });
            }

            @Override
            public int getItemCount() {
                return toolNames.length;
            }
        });
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = itemPadding;
                outRect.bottom = itemPadding;
            }
        });

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        dialog.setCanceledOnTouchOutside(true);
        hideStatusBar(dialog.getWindow(), false);
        dialog.show();
    }

    private void showOnLineUserListDialog(List<UserInfo> userInfoList) {
        if (userInfoList == null || userInfoList.size() <= 0) {
            return;
        }
        RelativeLayout dialogView = new RelativeLayout(this);
        LayoutInflater.from(this).inflate(R.layout.action_room_all_user_list, dialogView, true);
        RecyclerView recyclerView = dialogView.findViewById(R.id.live_room_action_sheet_all_user_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivUserIcon;
            final TextView tvUserName;
            final TextView tvInvite;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivUserIcon = itemView.findViewById(R.id.live_room_action_sheet_online_user_item_icon);
                tvUserName = itemView.findViewById(R.id.live_room_action_sheet_online_user_item_name);
                tvInvite = itemView.findViewById(R.id.live_room_action_sheet_online_user_item_status);
            }

        }
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.live_room_dialog);
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.action_room_online_user_invite_item, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                UserInfo info = userInfoList.get(position);

                Glide.with(holder.itemView)
                        .load(UserUtil.getUserProfileIcon(info.userId))
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(holder.ivUserIcon);

                holder.tvUserName.setText(info.userName);
                holder.tvInvite.setActivated(true);
                holder.tvInvite.setOnClickListener(v -> {
                    // 开始PK
                    startPKWith(info.userId);
                    dialog.dismiss();
                });
            }

            @Override
            public int getItemCount() {
                return userInfoList.size();
            }
        });

        dialog.setContentView(dialogView);
        dialog.setCanceledOnTouchOutside(true);
        hideStatusBar(dialog.getWindow(), false);
        dialog.show();
    }

    private void hideStatusBar(Window window, boolean darkText) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && darkText) {
            flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        window.getDecorView().setSystemUiVisibility(flag |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void showPKEndDialog() {
        new AlertDialog.Builder(HostActivity.this)
                .setTitle("PK")
                .setMessage("Currently in PK, whether to stop PK?")
                .setPositiveButton(R.string.cmm_ok, (dialog, which) -> {
                    dialog.dismiss();
                    stopPK();
                })
                .setNegativeButton(R.string.cmm_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void setupLocalFullView() {
        mDataBinding.flLocalFullContainer.setVisibility(View.VISIBLE);

        // remove old video view
        mDataBinding.flLocalFullContainer.removeAllViews();
        mDataBinding.ivLoadingBg.setVisibility(View.GONE);
        rtcManager.renderLocalVideo(mDataBinding.flLocalFullContainer);
    }

    private void setupRemoteView(int uid) {
        runOnUiThread(() -> {
            mDataBinding.remoteCallLayout.setVisibility(View.VISIBLE);
            rtcManager.renderRemoteVideo(mDataBinding.remoteCallVideoLayout, uid);
        });
    }

    private void resetRemoteView() {
        runOnUiThread(() -> {
            mDataBinding.remoteCallLayout.setVisibility(View.GONE);
            mDataBinding.remoteCallVideoLayout.removeAllViews();
        });
    }


    private String getLocalRoomId() {
        return mRoomInfo.roomId;
    }

    private void resetRemoteViewLayout(String remoteUserName) {
        runOnUiThread(() -> {
            mDataBinding.remoteCallPeerName.setText(remoteUserName);
            mDataBinding.remoteCallCloseBtn.setOnClickListener(v -> stopPK());
        });
    }

    //==========================RTCManager Logic===========================================

    private void initRTCManager() {
        rtcManager.init(this, getAgoraAppId(), new RtcManager.OnInitializeListener() {
            @Override
            public void onError(int code, String message) {
                if (!TextUtils.isEmpty(message)) {
                    runOnUiThread(() -> {
                        Toast.makeText(HostActivity.this, message, Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onUserJoined(int uid) {
                setupRemoteView(uid);
            }

            @Override
            public void onUserLeaved(int uid, int reason) {
                resetRemoteView();
            }
        });
    }

    private void startStreaming() {
        int mode = mRoomInfo.mode;
        if (mode == RoomInfo.PUSH_MODE_DIRECT_CDN) {
            startStreamingByCDN();
        } else {
            startStreamingByRtc();
        }
    }

    private void startStreamingByCDN() {
        rtcManager.startDirectCDNStreaming(getLocalRoomId());
    }

    private void startStreamingByRtc() {
        runOnUiThread(() -> rtcManager.startRtcStreaming(getLocalRoomId(), true));
    }

    private void startRTCPK() {
        if (mRoomInfo.mode == RoomInfo.PUSH_MODE_DIRECT_CDN) {
            rtcManager.stopDirectCDNStreaming(this::startStreamingByRtc);
        }
    }

    private void stopRTCPK() {
        if (mRoomInfo.mode == RoomInfo.PUSH_MODE_DIRECT_CDN) {
            rtcManager.stopRtcStreaming(mRoomInfo.roomId, this::startStreamingByCDN);
        }
    }

    private String getAgoraAppId() {
        String appId = getString(R.string.agora_app_id);
        if (TextUtils.isEmpty(appId)) {
            throw new RuntimeException("the app id is empty");
        }
        return appId;
    }


    //=========================SyncManager Logic===========================================

    private final SyncManager.EventListener roomEventListener = new SyncManager.EventListener() {
        @Override
        public void onCreated(IObject item) {
            runOnUiThread(() -> onRoomInfoChanged(item));
        }

        @Override
        public void onUpdated(IObject item) {
            runOnUiThread(() -> onRoomInfoChanged(item));
        }

        @Override
        public void onDeleted(IObject item) {

        }

        @Override
        public void onSubscribeError(SyncManagerException ex) {
            Toast.makeText(HostActivity.this, "initSyncManager subscribe error: " + ex.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    private final SyncManager.EventListener userCollectionEventListener = new SyncManager.EventListener() {
        @Override
        public void onCreated(IObject item) {
            UserInfo info = item.toObject(UserInfo.class);
            if (info.roomId.equals(mRoomInfo.roomId)) {
                // 用户进来
                RoomInfo roomInfo = new RoomInfo(mRoomInfo);
                roomInfo.userCount++;
                SyncManager.Instance()
                        .getScene(SYNC_DEFAULT_CHANNEL)
                        .collection(SYNC_COLLECTION_ROOM_INFO)
                        .document(mRoomInfo.objectId)
                        .update(roomInfo.toMap(), null);
            }
        }

        @Override
        public void onUpdated(IObject item) {

        }

        @Override
        public void onDeleted(IObject item) {
            UserInfo info = item.toObject(UserInfo.class);
            if (info.roomId.equals(mRoomInfo.roomId)) {
                // 用户离开
                RoomInfo roomInfo = new RoomInfo(mRoomInfo);
                roomInfo.userCount--;
                SyncManager.Instance()
                        .getScene(SYNC_DEFAULT_CHANNEL)
                        .collection(SYNC_COLLECTION_ROOM_INFO)
                        .document(mRoomInfo.objectId)
                        .update(roomInfo.toMap(), null);
            }
        }

        @Override
        public void onSubscribeError(SyncManagerException ex) {

        }
    };

    private void initSyncManager() {
        SyncManager.Instance()
                .getScene(mRoomInfo.roomId)
                .collection(SYNC_COLLECTION_ROOM_INFO)
                .document(mRoomInfo.objectId)
                .subscribe(roomEventListener);
        SyncManager.Instance()
                .getScene(mRoomInfo.roomId)
                .collection(SYNC_COLLECTION_USER_INFO)
                .subcribe(userCollectionEventListener);
    }

    private void onRoomInfoChanged(IObject item) {
        RoomInfo roomInfo = item.toObject(RoomInfo.class);
        RoomInfo oldRoomInfo = mRoomInfo;
        mRoomInfo = roomInfo;
        mRoomInfo.objectId = item.getId();
        boolean newPkStatue = !TextUtils.isEmpty(roomInfo.userIdPK);
        boolean oldPkStatue = !TextUtils.isEmpty(oldRoomInfo.userIdPK);

        if (newPkStatue != oldPkStatue) {
            if (newPkStatue) {
                // 开始PK
                Toast.makeText(this, "Start PK", Toast.LENGTH_LONG).show();
                startRTCPK();
                getUserInfoById(roomInfo.userIdPK, data -> {
                    resetRemoteViewLayout(data.userName);
                });
            } else {
                // 停止PK
                Toast.makeText(this, "Stop PK", Toast.LENGTH_LONG).show();
                stopRTCPK();
                resetRemoteView();
            }
        }

        int newUserCount = roomInfo.userCount;
        int oldUserCount = oldRoomInfo.userCount;
        if (newUserCount != oldUserCount) {
            // 更新房间内人数
            runOnUiThread(HostActivity.this::updateRoomUserCountTv);
        }
    }

    private void loadRoomUserInfoList() {
        SyncManager.Instance()
                .getScene(mRoomInfo.roomId)
                .collection(SYNC_COLLECTION_USER_INFO)
                .get(new SyncManager.DataListCallback() {
                    @Override
                    public void onSuccess(List<IObject> result) {
                        List<UserInfo> userInfos = new ArrayList<>();
                        for (IObject iObject : result) {
                            UserInfo item = iObject.toObject(UserInfo.class);
                            if (item.roomId.equals(mRoomInfo.roomId)) {
                                userInfos.add(item);
                            }
                        }
                        // 显示在线用户弹窗
                        runOnUiThread(() -> showOnLineUserListDialog(userInfos));

                    }

                    @Override
                    public void onFail(SyncManagerException ex) {
                        Toast.makeText(HostActivity.this, "loadRoomUserInfoList get error: " + ex.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void startPKWith(String pkUserId) {
        RoomInfo newRoomInfo = new RoomInfo(mRoomInfo);
        newRoomInfo.userIdPK = pkUserId;
        SyncManager.Instance()
                .getScene(mRoomInfo.roomId)
                .collection(SYNC_COLLECTION_ROOM_INFO)
                .document(mRoomInfo.objectId)
                .update(newRoomInfo.toMap(), new SyncManager.DataItemCallback() {
                    @Override
                    public void onSuccess(IObject result) {

                    }

                    @Override
                    public void onFail(SyncManagerException exception) {

                    }
                });
    }

    private void stopPK() {
        RoomInfo newRoomInfo = new RoomInfo(mRoomInfo);
        newRoomInfo.userIdPK = "";
        SyncManager.Instance()
                .getScene(mRoomInfo.roomId)
                .collection(SYNC_COLLECTION_ROOM_INFO)
                .document(mRoomInfo.objectId)
                .update(newRoomInfo.toMap(), new SyncManager.DataItemCallback() {
                    @Override
                    public void onSuccess(IObject result) {

                    }

                    @Override
                    public void onFail(SyncManagerException exception) {

                    }
                });
    }


    private void checkIsPKing(Runnable idle, DataCallback<String> pking) {
        if (!TextUtils.isEmpty(mRoomInfo.userIdPK)) {
            if (pking != null) {
                pking.onSuccess(mRoomInfo.userIdPK);
            }
        } else {
            if (idle != null) {
                idle.run();
            }
        }
    }

    private void cleanCurrRoomInfo(Runnable success) {
        SyncManager.Instance()
                .unsubscribe(roomEventListener);
        SyncManager.Instance()
                .unsubscribe(userCollectionEventListener);
        SyncManager.Instance()
                .getScene(mRoomInfo.roomId)
                .delete(new SyncManager.Callback() {
                    @Override
                    public void onSuccess() {
                        SyncManager.Instance()
                                .getScene(mRoomInfo.roomId)
                                .collection(SYNC_COLLECTION_ROOM_INFO)
                                .document(mRoomInfo.objectId)
                                .delete(new SyncManager.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        if (success != null) {
                                            success.run();
                                        }
                                    }

                                    @Override
                                    public void onFail(SyncManagerException exception) {
                                        if (success != null) {
                                            success.run();
                                        }
                                        Toast.makeText(HostActivity.this, "deleteRoomInfo failed exception: " + exception.toString(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    @Override
                    public void onFail(SyncManagerException exception) {
                        Toast.makeText(HostActivity.this, "deleteRoomInfo failed exception: " + exception.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getUserInfoById(String userId, DataCallback<UserInfo> callback) {
        SyncManager.Instance()
                .getScene(SYNC_DEFAULT_CHANNEL)
                .collection(SYNC_COLLECTION_USER_INFO)
                .get(new SyncManager.DataListCallback() {
                    @Override
                    public void onSuccess(List<IObject> result) {
                        for (IObject iObject : result) {
                            UserInfo info = iObject.toObject(UserInfo.class);
                            if (info.userId.equals(userId)) {
                                if (callback != null) {
                                    callback.onSuccess(info);
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void onFail(SyncManagerException exception) {

                    }
                });
    }

}
