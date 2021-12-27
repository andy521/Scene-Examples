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
import androidx.annotation.Nullable;
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

import io.agora.example.base.BaseActivity;
import io.agora.superapp.R;
import io.agora.superapp.databinding.ActivityVideoBinding;
import io.agora.superapp.manager.RtcManager;
import io.agora.superapp.model.RoomInfo;
import io.agora.superapp.model.UserInfo;
import io.agora.superapp.util.DataCallback;
import io.agora.superapp.util.UserUtil;
import io.agora.syncmanager.rtm.IObject;
import io.agora.syncmanager.rtm.SceneReference;
import io.agora.syncmanager.rtm.Sync;
import io.agora.syncmanager.rtm.SyncManagerException;

import static io.agora.superapp.Constants.SYNC_COLLECTION_USER_INFO;

public class HostActivity extends BaseActivity<ActivityVideoBinding> {
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRoomInfo = ((RoomInfo) getIntent().getSerializableExtra(EXTRA_ROOM_INFO));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(mRoomInfo.roomName);

        int userProfileIcon = UserUtil.getUserProfileIcon(getLocalRoomId());

        mBinding.ivLoadingBg.setVisibility(View.VISIBLE);
        mBinding.ivLoadingBg.setImageResource(userProfileIcon);

        Glide.with(this)
                .load(userProfileIcon)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(mBinding.ivRoomAvatar);

        mBinding.tvRoomName.setText(String.format(Locale.US, "%s(%s)", mRoomInfo.roomName, mRoomInfo.roomId));
        updateRoomUserCountTv();

        mBinding.liveBottomBtnMore.setOnClickListener(v -> {
            showMoreChoiceDialog();
        });

        mBinding.liveBottomBtnClose.setOnClickListener(v -> onBackPressed());
        mBinding.llParticipant.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mRoomInfo.userIdPK)) {
                loadRoomUserInfoList();
            }else{
                Toast.makeText(this, "The current room is linking", Toast.LENGTH_SHORT).show();
            }
        });

        initSyncManager();

        initRTCManager();
        setupLocalFullView();
        startStreaming();
    }


    private void updateRoomUserCountTv() {
        mBinding.liveParticipantCountText.setText(mRoomInfo.userCount + "");
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        checkIsPKing(getLocalRoomId(),
                () -> cleanCurrRoomInfo(this::finish),
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

        final class ViewHolder extends RecyclerView.ViewHolder{
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
                switch (position){
                    case 1:
                        // 静音
                        holder.iconIv.setActivated(!RtcManager.isMuteLocalAudio);
                        break;
                }

                holder.iconIv.setOnClickListener(v -> {
                    switch (position){
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
        mBinding.flLocalFullContainer.setVisibility(View.VISIBLE);

        // remove old video view
        mBinding.flLocalFullContainer.removeAllViews();
        mBinding.ivLoadingBg.setVisibility(View.GONE);
        rtcManager.renderLocalVideo(mBinding.flLocalFullContainer, () -> mBinding.ivLoadingBg.setVisibility(View.GONE));
    }

    private void setupRemoteView(int uid) {
        runOnUiThread(() -> {
            mBinding.remoteCallLayout.setVisibility(View.VISIBLE);
            rtcManager.renderRemoteVideo(mBinding.remoteCallVideoLayout, uid, () -> runOnUiThread(() -> {

            }));
        });
    }

    private void resetRemoteView(){
        runOnUiThread(() -> {
            mBinding.remoteCallLayout.setVisibility(View.GONE);
            mBinding.remoteCallVideoLayout.removeAllViews();
        });
    }


    private String getLocalRoomId() {
        return mRoomInfo.roomId;
    }

    private void resetRemoteViewLayout(String remoteUserName){
        runOnUiThread(() -> {
            mBinding.remoteCallPeerName.setText(remoteUserName);
            mBinding.remoteCallCloseBtn.setOnClickListener(v -> stopPK());
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
        if(mRoomInfo.mode == RoomInfo.PUSH_MODE_DIRECT_CDN){
            rtcManager.stopDirectCDNStreaming(this::startStreamingByRtc);
        }
    }

    private void stopRTCPK() {
        if(mRoomInfo.mode == RoomInfo.PUSH_MODE_DIRECT_CDN){
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

    private SceneReference mRoomSceneRef;

    private void initSyncManager() {
        Sync.Instance().joinScene(mRoomInfo.roomId, new Sync.JoinSceneCallback() {
            @Override
            public void onSuccess(SceneReference sceneReference) {
                mRoomSceneRef = sceneReference;
                mRoomSceneRef.subscribe(new Sync.EventListener() {
                    @Override
                    public void onCreated(IObject item) {

                    }

                    @Override
                    public void onUpdated(IObject item) {
                        runOnUiThread(() -> onRoomInfoChanged(item));
                    }

                    @Override
                    public void onDeleted(IObject item) {
                        finish();
                    }

                    @Override
                    public void onSubscribeError(SyncManagerException ex) {
                        Toast.makeText(HostActivity.this, "initSyncManager subscribe error: " + ex.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

                mRoomSceneRef.collection(SYNC_COLLECTION_USER_INFO)
                        .subscribe(new Sync.EventListener() {
                            @Override
                            public void onCreated(IObject item) {
                                UserInfo info = item.toObject(UserInfo.class);
                                if (info.roomId.equals(mRoomInfo.roomId)) {
                                    // 用户进来
                                    RoomInfo roomInfo = new RoomInfo(mRoomInfo);
                                    roomInfo.userCount++;
                                    mRoomSceneRef.update(roomInfo.toObjectMap(), null);
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
                                    mRoomSceneRef.update(roomInfo.toObjectMap(), null);
                                }
                            }

                            @Override
                            public void onSubscribeError(SyncManagerException ex) {

                            }
                        });
            }

            @Override
            public void onFail(SyncManagerException exception) {
                finish();
            }
        });
    }

    private void onRoomInfoChanged(IObject item) {
        RoomInfo roomInfo = item.toObject(RoomInfo.class);
        if(!roomInfo.roomId.equals(mRoomInfo.roomId)){
            return;
        }
        RoomInfo oldRoomInfo = mRoomInfo;
        mRoomInfo = roomInfo;
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
        if(mRoomSceneRef == null){
            return;
        }
        mRoomSceneRef.collection(SYNC_COLLECTION_USER_INFO)
                .get(new Sync.DataListCallback() {
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
        checkIsPKing(pkUserId,
                () -> {
                    RoomInfo newRoomInfo = new RoomInfo(mRoomInfo);
                    newRoomInfo.userIdPK = pkUserId;
                    if(mRoomSceneRef != null){
                        mRoomSceneRef.update(newRoomInfo.toObjectMap(), new Sync.DataItemCallback() {
                            @Override
                            public void onSuccess(IObject result) {

                            }

                            @Override
                            public void onFail(SyncManagerException exception) {

                            }
                        });

                    }

                },
                data -> Toast.makeText(HostActivity.this, "The host " + pkUserId + " is kping with " + data, Toast.LENGTH_LONG).show());
    }

    private void stopPK() {
        RoomInfo newRoomInfo = new RoomInfo(mRoomInfo);
        newRoomInfo.userIdPK = "";
        mRoomSceneRef.update(newRoomInfo.toObjectMap(), new Sync.DataItemCallback() {
            @Override
            public void onSuccess(IObject result) {

            }

            @Override
            public void onFail(SyncManagerException exception) {

            }
        });
    }


    private void checkIsPKing(String channelId, Runnable idle, DataCallback<String> pking) {
        if (channelId.equals(mRoomInfo.roomId)) {
            if (!TextUtils.isEmpty(mRoomInfo.userIdPK)) {
                if (pking != null) {
                    pking.onSuccess(mRoomInfo.userIdPK);
                }
            } else {
                if (idle != null) {
                    idle.run();
                }
            }
        } else {
            Sync.Instance()
                    .getScenes(new Sync.DataListCallback() {
                        @Override
                        public void onSuccess(List<IObject> result) {
                            RoomInfo pkItem = null;
                            for (IObject iObject : result) {
                                RoomInfo item = iObject.toObject(RoomInfo.class);
                                if (channelId.equals(item.userIdPK)) {
                                    pkItem = item;
                                    break;
                                }
                            }
                            if (pkItem != null) {
                                if (pking != null) {
                                    pking.onSuccess(pkItem.roomId);
                                }
                            } else {
                                if (idle != null) {
                                    idle.run();
                                }
                            }
                        }

                        @Override
                        public void onFail(SyncManagerException exception) {
                            Toast.makeText(HostActivity.this, exception.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void cleanCurrRoomInfo(Runnable success) {
        if(mRoomSceneRef == null){
            if (success != null) {
                success.run();
            }
            return;
        }
        mRoomSceneRef.delete(new Sync.Callback() {
            @Override
            public void onSuccess() {
                if (success != null) {
                    success.run();
                }
            }

            @Override
            public void onFail(SyncManagerException exception) {
                Toast.makeText(HostActivity.this, "deleteRoomInfo failed exception: " + exception.toString(), Toast.LENGTH_LONG).show();

            }
        });
    }

    private void getUserInfoById(String userId, DataCallback<UserInfo> callback) {
        if(mRoomSceneRef == null){
            return;
        }
        mRoomSceneRef.collection(SYNC_COLLECTION_USER_INFO)
                .get(new Sync.DataListCallback() {
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
