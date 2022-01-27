package io.agora.scene.virtualimage.ui.room;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Objects;

import io.agora.example.base.BaseUtil;
import io.agora.rtc2.RtcEngine;
import io.agora.scene.virtualimage.R;
import io.agora.scene.virtualimage.bean.AgoraGame;
import io.agora.scene.virtualimage.bean.GameInfo;
import io.agora.scene.virtualimage.bean.LocalUser;
import io.agora.scene.virtualimage.bean.RoomInfo;
import io.agora.scene.virtualimage.manager.FUManager;
import io.agora.scene.virtualimage.manager.RtcManager;
import io.agora.scene.virtualimage.repo.GameRepo;
import io.agora.scene.virtualimage.util.OneConstants;
import io.agora.scene.virtualimage.util.OneSyncEventListener;
import io.agora.scene.virtualimage.util.ViewStatus;
import io.agora.syncmanager.rtm.IObject;
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
    // 当前在玩游戏信息
    @Nullable
    public AgoraGame currentGame;
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
    private final MutableLiveData<LocalUser> _targetUser = new MutableLiveData<>();
    // 当前游戏信息
    private final MutableLiveData<GameInfo> _gameInfo = new MutableLiveData<>();
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
                if (!amHost)
                    // 副主播只能与房主连线
                    _targetUser.postValue(new LocalUser(channelId, currentRoom.getUserId()));
            }

            @Override
            public void onUserJoined(String channelId, int uid) {
                BaseUtil.logD("onUserJoined:" + uid);
                if (!amHost) return;
                if (_targetUser.getValue() == null)
                    _targetUser.postValue(new LocalUser(channelId, String.valueOf(uid)));
            }

            @Override
            public void onUserOffline(String channelId, int uid) {
                BaseUtil.logD("onUserOffline:" + uid);
                if (_targetUser.getValue() != null) {
                    if (_targetUser.getValue().getUserId().equals(String.valueOf(uid)))
                        _targetUser.postValue(null);
                }
                if (currentGame != null){
                    requestEndGame();
                }
            }
        });
    }

    private void onJoinRTMSucceed(@NonNull SceneReference sceneReference) {
        BaseUtil.logD("RTM ready");
        currentSceneRef = sceneReference;
        subscribeAttr();
    }

    private void subscribeAttr() {
        if (currentSceneRef != null) {
            if (amHost)
                currentSceneRef.update(OneConstants.GAME_INFO, new GameInfo(GameInfo.END, -1), null);
            else
                currentSceneRef.get(OneConstants.GAME_INFO, (GetAttrCallback) this::handleGetGameInfo);
            currentSceneRef.subscribe(OneConstants.GAME_INFO, new OneSyncEventListener(OneConstants.GAME_INFO, this::handleGameInfo));
        }
    }

    private void handleGetGameInfo(@Nullable IObject iObject) {
        GameInfo gameInfo = tryHandleIObject(iObject, GameInfo.class);
        if (gameInfo != null) {
            switch (gameInfo.getStatus()) {
                case GameInfo.IDLE:
                    break;
                case GameInfo.START: {
                    currentGame = GameRepo.getGameDetail(gameInfo.getGameId());
                    _gameInfo.postValue(gameInfo);
                    break;
                }
                case GameInfo.END: {
                    currentGame = null;
                    break;
                }
            }
        }
    }

    /**
     * 游戏 未开始：不做任何操作
     * 已开始：加载
     * 结束： 发送网络请求
     */
    private void handleGameInfo(@Nullable IObject iObject) {
        GameInfo gameInfo = tryHandleIObject(iObject, GameInfo.class);
        if (gameInfo != null) {
            switch (gameInfo.getStatus()) {
                case GameInfo.IDLE:
                    break;
                case GameInfo.START: {
                    currentGame = GameRepo.getGameDetail(gameInfo.getGameId());
                    _gameInfo.postValue(gameInfo);
                    break;
                }
                case GameInfo.END: {
                    _gameInfo.postValue(gameInfo);
                    if (currentGame != null) {
                        GameRepo.endThisGame(currentRoom, currentGame);
                        currentGame = null;
                    }
                    if (currentSceneRef != null)
                        currentSceneRef.update(OneConstants.GAME_INFO, new GameInfo(GameInfo.IDLE, gameInfo.getGameId()), (GetAttrCallback) this::handleGameInfo);
                    break;
                }
            }
        }
    }

    @Nullable
    private <T> T tryHandleIObject(@Nullable IObject result, @NonNull Class<T> modelClass) {
        if (result == null) return null;
        T obj = null;
        try {
            obj = result.toObject(modelClass);
        } catch (Exception ignored) {
        }
        return obj;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        // destroy RTE
        RtcManager.getInstance().reset(true);
        FUManager.getInstance().stop();

        new Thread(() -> {
            if (currentGame != null) {
                GameRepo.endThisGame(currentRoom, currentGame);
            }
            // destroy RTM
            if (currentSceneRef != null) {
                if (amHost)
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
                else currentSceneRef.unsubscribe(null);
            }
        }).start();
    }
    //</editor-fold>

    @NonNull
    public LiveData<ViewStatus> viewStatus() {
        return _viewStatus;
    }

    @NonNull
    public LiveData<LocalUser> targetUser() {
        return _targetUser;
    }

    @NonNull
    public LiveData<GameInfo> gameInfo() {
        return _gameInfo;
    }

    public void requestStartGame(@NonNull AgoraGame agoraGame) {
        if (currentSceneRef != null) {
            GameInfo gameInfo = new GameInfo(GameInfo.START, agoraGame.getGameId());
            currentSceneRef.update(OneConstants.GAME_INFO, gameInfo, null);
        }
    }

    public void requestEndGame() {
        if (currentSceneRef != null && currentGame != null) {
            currentSceneRef.update(OneConstants.GAME_INFO, new GameInfo(GameInfo.END, currentGame.getGameId()), null);
        }
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

    private interface GetAttrCallback extends Sync.DataItemCallback {
        @Override
        default void onFail(SyncManagerException exception) {

        }
    }
}
