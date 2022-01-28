package io.agora.scene.virtualimage.ui.room;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.agora.example.base.BaseUtil;
import io.agora.rtc2.RtcEngine;
import io.agora.scene.virtualimage.R;
import io.agora.scene.virtualimage.bean.LocalUser;
import io.agora.scene.virtualimage.bean.RoomInfo;
import io.agora.scene.virtualimage.manager.FUManager;
import io.agora.scene.virtualimage.manager.RtcManager;
import io.agora.scene.virtualimage.util.ViewStatus;
import io.agora.syncmanager.rtm.SceneReference;
import io.agora.syncmanager.rtm.Sync;
import io.agora.syncmanager.rtm.SyncManagerException;


/**
 * @author lq
 */
@Keep
public class RoomViewModel extends ViewModel {

    //<editor-fold desc="Persistent variable">
    // 当前用户是否为主播
    public final boolean amHost;
    // 当前房间信息
    public final RoomInfo currentRoom;
    // 当前用户信息
    @NonNull
    public final LocalUser localUser;
    // SyncManager 必须
    @Nullable
    public SceneReference currentSceneRef = null;
    //</editor-fold>


    //<editor-fold desc="Live data">
    // UI状态
    private final MutableLiveData<ViewStatus> _viewStatus = new MutableLiveData<>();
    // 对方主播信息
    private final MutableLiveData<List<LocalUser>> _targetUser = new MutableLiveData<>();
    private List<LocalUser> targetUserList = new ArrayList<>();
    // RTC 音频状态
    public final MutableLiveData<Boolean> isLocalMicMuted = new MutableLiveData<>(false);
    //</editor-fold>

    //<editor-fold desc="Init and end">
    public RoomViewModel(@NonNull Context context, @NonNull LocalUser localUser, @NonNull RoomInfo roomInfo) {
        this.currentRoom = roomInfo;
        this.localUser = localUser;

        this.amHost = Objects.equals(currentRoom.getUserId(), localUser.getUserId());


        initRTC(context, new RtcManager.OnChannelListener() {
            @Override
            public void onError(int code, String message) {
                BaseUtil.logD("err: " + code + " ==> " + RtcEngine.getErrorDescription(code));
            }

            @Override
            public void onJoinSuccess(String channelId, int uid) {
                BaseUtil.logD("onJoinChannelSuccess:" + uid);
            }

            @Override
            public void onUserJoined(String channelId, int uid) {
                BaseUtil.logD("onUserJoined:" + uid);
                targetUserList = new ArrayList<>(targetUserList);
                targetUserList.add(new LocalUser(channelId, String.valueOf(uid)));
                _targetUser.postValue(targetUserList);
            }

            @Override
            public void onUserOffline(String channelId, int uid) {
                BaseUtil.logD("onUserOffline:" + uid);
                targetUserList = new ArrayList<>(targetUserList);
                Iterator<LocalUser> iterator = targetUserList.iterator();
                while (iterator.hasNext()){
                    LocalUser user = iterator.next();
                    if(user.getUserId().equals(String.valueOf(uid))){
                        iterator.remove();
                    }
                }
                _targetUser.postValue(targetUserList);
            }
        });
    }

    private void onJoinRTMSucceed(@NonNull SceneReference sceneReference) {
        BaseUtil.logD("RTM ready");
        currentSceneRef = sceneReference;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        targetUserList.clear();
        // destroy RTE
        RtcManager.getInstance().reset(true);
        FUManager.getInstance().stop();

        if (currentSceneRef != null) {
            if (amHost){
                currentSceneRef.delete(new Sync.Callback() {
                    @Override
                    public void onSuccess() {
                        BaseUtil.logD("delete onSuccess");
                    }

                    @Override
                    public void onFail(SyncManagerException exception) {
                        BaseUtil.logD("delete onFail");
                    }
                });
            }
            else {
                currentSceneRef.unsubscribe(null);
            }
        }
    }
    //</editor-fold>

    @NonNull
    public LiveData<ViewStatus> viewStatus() {
        return _viewStatus;
    }

    @NonNull
    public LiveData<List<LocalUser>> targetUser() {
        return _targetUser;
    }

    public void initRTM() {
        new Thread(() -> Sync.Instance().joinScene(currentRoom.getId(), new Sync.JoinSceneCallback() {
            @Override
            public void onSuccess(SceneReference sceneReference) {
                BaseUtil.logD("加入频道成功");
                onJoinRTMSucceed(sceneReference);
            }

            @Override
            public void onFail(SyncManagerException e) {
                BaseUtil.logD("加入频道失败");
                _viewStatus.postValue(new ViewStatus.Error("加入RTM失败"));
            }
        })).start();
    }

    //<editor-fold desc="RTC related">

    public void toggleMute() {
        boolean isMute = isLocalMicMuted.getValue() == Boolean.TRUE;
        isLocalMicMuted.setValue(!isMute);
        RtcManager.getInstance().muteLocalAudio(!isMute);
    }

    public void joinRoom(Context context, RtcManager.OnChannelListener listener) {
        RtcManager.getInstance().joinChannel(currentRoom.getId(), localUser.getUserId(),
                context.getString(R.string.rtc_app_token),
                true, listener);
    }

    public void initRTC(@NonNull Context mContext, @NonNull RtcManager.OnChannelListener channelListener) {
        joinRoom(mContext, channelListener);
        initRTM();
    }

    public void setupLocalView(@NonNull FrameLayout view) {
        RtcManager.getInstance().setOnMediaOptionUpdateListener(new RtcManager.OnMediaOptionUpdateListener() {
            @Override
            public void onMediaOptionUpdated() {
                if (RtcManager.getInstance().isPublishAvatarTrack()) {
                    RtcManager.getInstance().renderLocalAvatarVideo(view);
                }else{
                    RtcManager.getInstance().renderLocalCameraVideo(view);
                }
            }
        });
        FUManager.getInstance().start();
    }

    /**
     * @param view 用来构造 videoCanvas
     */
    public void setupRemoteView(@NonNull FrameLayout view, String channelId, int uid) {
        RtcManager.getInstance().renderRemoteVideo(view, channelId, uid);
    }

    //</editor-fold>

}
