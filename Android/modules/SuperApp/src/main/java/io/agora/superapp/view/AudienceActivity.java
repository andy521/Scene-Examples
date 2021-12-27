package io.agora.superapp.view;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

import io.agora.example.base.BaseActivity;
import io.agora.superapp.R;
import io.agora.superapp.databinding.ActivityVideoBinding;
import io.agora.superapp.manager.RtcManager;
import io.agora.superapp.model.RoomInfo;
import io.agora.superapp.model.UserInfo;
import io.agora.superapp.util.UserUtil;
import io.agora.syncmanager.rtm.IObject;
import io.agora.syncmanager.rtm.SceneReference;
import io.agora.syncmanager.rtm.Sync;
import io.agora.syncmanager.rtm.SyncManagerException;

import static io.agora.superapp.Constants.SYNC_COLLECTION_ROOM_INFO;
import static io.agora.superapp.Constants.SYNC_COLLECTION_USER_INFO;
import static io.agora.superapp.Constants.SYNC_SCENE_ID;

public class AudienceActivity extends BaseActivity<ActivityVideoBinding> {
    private static final String TAG = "AudienceActivity";

    private static final String EXTRA_ROOM_INFO = "roomInfo";

    private final RtcManager rtcManager = new RtcManager();
    private RoomInfo mRoomInfo;
    private UserInfo mUserInfo;

    public static Intent launch(Context context, RoomInfo roomInfo) {
        Intent intent = new Intent(context, AudienceActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRoomInfo = (RoomInfo)getIntent().getSerializableExtra(EXTRA_ROOM_INFO);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(mRoomInfo.roomName);

        int userProfileIcon = UserUtil.getUserProfileIcon(mRoomInfo.roomId);

        mBinding.ivLoadingBg.setVisibility(View.VISIBLE);
        mBinding.ivLoadingBg.setImageResource(userProfileIcon);

        Glide.with(this)
                .load(userProfileIcon)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(mBinding.ivRoomAvatar);

        mBinding.tvRoomName.setText(String.format(Locale.US, "%s(%s)", mRoomInfo.roomName, mRoomInfo.roomId));
        mBinding.liveBottomBtnMore.setVisibility(View.GONE);
        updateRoomUserCountTv();

        mBinding.remoteCallCloseBtn.setOnClickListener(v -> {
            stopPK();
        });

        mBinding.liveBottomBtnClose.setOnClickListener(v -> onBackPressed());
        mBinding.liveBottomBtnMore.setOnClickListener(v-> showMoreChoiceDialog());

        initRTCManager();
        initSyncManager();
        setupVideoPlayer();
    }

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


    private void setupVideoPlayer(){
        mBinding.flLocalFullContainer.setVisibility(View.VISIBLE);
        // remove old video view
        mBinding.flLocalFullContainer.removeAllViews();

        mBinding.ivLoadingBg.setVisibility(View.GONE);
        rtcManager.renderPlayerView(mBinding.flLocalFullContainer,  () -> {
            runOnUiThread(() -> mBinding.ivLoadingBg.setVisibility(View.GONE));
        });
        rtcManager.openPlayerSrc(mRoomInfo.roomId, mRoomInfo.mode == RoomInfo.PUSH_MODE_DIRECT_CDN && !checkRoomIsPKing());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcManager.release();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(checkMeIsPKing()){
            showPKEndDialog();
        }else{
            leaveRoom(this::finish);
        }
    }

    private void updateRoomUserCountTv(){
        mBinding.liveParticipantCountText.setText(mRoomInfo.userCount + "");
    }

    private void showPKEndDialog() {
        new AlertDialog.Builder(AudienceActivity.this)
                .setTitle("PK")
                .setMessage("Currently in PK, whether to stop PK?")
                .setPositiveButton(R.string.cmm_ok, (dialog, which) -> {
                    dialog.dismiss();
                    stopPK();
                })
                .setNegativeButton(R.string.cmm_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }


    //============================RTCManager Logic======================================


    private void initRTCManager(){
        rtcManager.init(this, getAgoraAppId(), new RtcManager.OnInitializeListener() {
            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onUserJoined(int uid) {
                runOnUiThread(() -> {
                    mBinding.remoteCallLayout.setVisibility(View.VISIBLE);
                    rtcManager.renderRemoteVideo(mBinding.remoteCallVideoLayout, uid, null);
                });
            }

            @Override
            public void onUserLeaved(int uid, int reason) {

            }
        });
    }

    private void startRTCPK() {
        rtcManager.startRtcStreaming(mRoomInfo.roomId, false);
        runOnUiThread(() -> {
            mBinding.liveBottomBtnMore.setVisibility(View.VISIBLE);
            mBinding.flLocalFullContainer.removeAllViews();
            mBinding.ivLoadingBg.setVisibility(View.GONE);
            rtcManager.renderLocalVideo(mBinding.flLocalFullContainer, null);
        });
    }

    private void stopRTCPK(){
        rtcManager.stopRtcStreaming(mRoomInfo.roomId, null);
        runOnUiThread(() -> {
            mBinding.liveBottomBtnMore.setVisibility(View.GONE);
            mBinding.remoteCallVideoLayout.removeAllViews();
            mBinding.remoteCallLayout.setVisibility(View.GONE);

            mBinding.flLocalFullContainer.removeAllViews();
            mBinding.ivLoadingBg.setVisibility(View.GONE);
            rtcManager.renderPlayerView(mBinding.flLocalFullContainer, null);
        });
    }

    private String getAgoraAppId() {
        String appId = getString(R.string.agora_app_id);
        if (TextUtils.isEmpty(appId)) {
            throw new RuntimeException("the app id is empty");
        }
        return appId;
    }


    //============================SyncManager Logic======================================

    private SceneReference mRoomSceneRef;

    private void initSyncManager(){
        Sync.Instance().joinScene(mRoomInfo.roomId, new Sync.JoinSceneCallback() {
            @Override
            public void onSuccess(SceneReference sceneReference) {
                mRoomSceneRef = sceneReference;
                enterRoom();
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
                        if(item.getId().equals(mRoomInfo.roomId)){
                            runOnUiThread(() -> {
                                new AlertDialog.Builder(AudienceActivity.this)
                                        .setTitle(R.string.cmm_tip)
                                        .setMessage(R.string.audience_leave_tip)
                                        .setPositiveButton(R.string.cmm_leave, (dialog, which) -> {
                                            dialog.dismiss();
                                            finish();
                                        })
                                        .setNegativeButton(R.string.cmm_cancel, (dialog, which) -> dialog.dismiss())
                                        .show();
                            });
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
                if (mUserInfo != null && mUserInfo.userId.equals(roomInfo.userIdPK)) {
                    Toast.makeText(this, "Start PK", Toast.LENGTH_LONG).show();
                    startRTCPK();
                }
            } else {
                // 停止PK
                if(mUserInfo != null && mUserInfo.userId.equals(oldRoomInfo.userIdPK)){
                    Toast.makeText(this, "Stop PK", Toast.LENGTH_LONG).show();
                    stopRTCPK();
                }
            }
        }

        int newUserCount = roomInfo.userCount;
        int oldUserCount = oldRoomInfo.userCount;
        if(newUserCount != oldUserCount){
            // 更新房间内人数
            runOnUiThread(this::updateRoomUserCountTv);
        }
    }

    private void enterRoom() {
        if(mRoomSceneRef == null){
            return;
        }
        mUserInfo = new UserInfo();
        mUserInfo.roomId = mRoomInfo.roomId;
        mUserInfo.userId = UserUtil.getLocalUserId();
        mUserInfo.userName = UserUtil.getLocalUserName(this);
        mRoomSceneRef
                .collection(SYNC_COLLECTION_USER_INFO)
                .add(mUserInfo.toMap(), new Sync.DataItemCallback() {
                    @Override
                    public void onSuccess(IObject result) {

                    }

                    @Override
                    public void onFail(SyncManagerException exception) {
                        Toast.makeText(AudienceActivity.this, exception.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void leaveRoom(Runnable success){
        if(mUserInfo == null || mRoomSceneRef == null){
            if(success != null){
                success.run();
            }
            return;
        }
        mRoomSceneRef
                .collection(SYNC_COLLECTION_USER_INFO)
                .document(mUserInfo.userId)
                .delete(new Sync.Callback() {
                    @Override
                    public void onSuccess() {
                        if(success != null){
                            success.run();
                        }
                    }

                    @Override
                    public void onFail(SyncManagerException exception) {
                        Toast.makeText(AudienceActivity.this, exception.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void stopPK() {
        if(mRoomSceneRef == null){
            return;
        }
        RoomInfo newRoomInfo = new RoomInfo(mRoomInfo);
        newRoomInfo.userIdPK = "";
        mRoomSceneRef
                .update(newRoomInfo.toObjectMap(), new Sync.DataItemCallback() {
                    @Override
                    public void onSuccess(IObject result) {

                    }

                    @Override
                    public void onFail(SyncManagerException exception) {

                    }
                });
    }

    private boolean checkMeIsPKing() {
        return !TextUtils.isEmpty(mUserInfo.userId) && mUserInfo.userId.equals(mRoomInfo.userIdPK);
    }

    private boolean checkRoomIsPKing() {
        return !TextUtils.isEmpty(mRoomInfo.userIdPK);
    }
}
