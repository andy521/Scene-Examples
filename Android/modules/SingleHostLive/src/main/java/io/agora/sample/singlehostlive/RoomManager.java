package io.agora.sample.singlehostlive;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.DrawableRes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.agora.syncmanager.rtm.IObject;
import io.agora.syncmanager.rtm.Scene;
import io.agora.syncmanager.rtm.SceneReference;
import io.agora.syncmanager.rtm.Sync;
import io.agora.syncmanager.rtm.SyncManagerException;
import io.agora.uiwidget.utils.RandomUtil;

public class RoomManager {

    private static volatile RoomManager INSTANCE;
    private static volatile boolean isInitialized = false;

    public static RoomManager getInstance(){
        if(INSTANCE== null){
            synchronized (RoomManager.class){
                if(INSTANCE == null){
                    INSTANCE = new RoomManager();
                }
            }
        }
        return INSTANCE;
    }

    private RoomManager(){}

    public void init(Context context, String appId, String token){
        if(isInitialized){
            return;
        }
        isInitialized = true;
        HashMap<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("token", token);
        params.put("defaultChannel", "signleLive");
        Sync.Instance().init(context, params, new Sync.Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail(SyncManagerException exception) {
                isInitialized = false;
            }
        });
    }

    public void createRoom(String roomName, DataCallback<RoomInfo> callback){
        RoomInfo roomInfo = new RoomInfo(roomName);
        Scene room = new Scene();
        room.setId(roomInfo.roomId);
        room.setUserId(roomInfo.userId);
        room.setProperty(roomInfo.toMap());
        Sync.Instance().createScene(room, new Sync.Callback() {
            @Override
            public void onSuccess() {
                if(callback != null){
                    callback.onSuccess(roomInfo);
                }
            }

            @Override
            public void onFail(SyncManagerException exception) {
                if(callback != null){
                    callback.onFailed(exception);
                }
            }
        });
    }

    public void getAllRooms(DataListCallback<RoomInfo> callback){
        Sync.Instance().getScenes(new Sync.DataListCallback() {
            @Override
            public void onSuccess(List<IObject> result) {
                if(callback != null){
                    List<RoomInfo> ret = new ArrayList<>();

                    try {
                        for (IObject iObject : result) {
                            RoomInfo item = iObject.toObject(RoomInfo.class);
                            ret.add(item);
                        }
                        callback.onSuccess(ret);
                    } catch (Exception exception) {
                        callback.onFailed(exception);
                    }

                }
            }

            @Override
            public void onFail(SyncManagerException exception) {
                if(callback != null){
                    callback.onFailed(exception);
                }
            }
        });
    }

    public void joinRoom(String roomId){
        Sync.Instance().joinScene(roomId, new Sync.JoinSceneCallback() {
            @Override
            public void onSuccess(SceneReference sceneReference) {

            }

            @Override
            public void onFail(SyncManagerException exception) {

            }
        });
    }

    public static class RoomInfo implements Serializable {
        public String roomName;
        public String roomId = RandomUtil.randomId() + 10000 + "";
        public String userId = RandomUtil.randomId() + 10000 + "";
        // 和ios对应上：portrait[01-14]
        public String backgroundId = String.format(Locale.US, "portrait%02d", RandomUtil.randomId(1, 14));

        public RoomInfo(String roomName) {
            this.roomName = roomName;
        }

        public int getAndroidBgId(){
            if(TextUtils.isEmpty(backgroundId)){
                return 0;
            }
            int bgResId = 0;
            switch (backgroundId){
                case "portrait01": bgResId = R.drawable.user_profile_image_1; break;
                case "portrait02": bgResId = R.drawable.user_profile_image_2; break;
                case "portrait03": bgResId = R.drawable.user_profile_image_3; break;
                case "portrait04": bgResId = R.drawable.user_profile_image_4; break;
                case "portrait05": bgResId = R.drawable.user_profile_image_5; break;
                case "portrait06": bgResId = R.drawable.user_profile_image_6; break;
                case "portrait07": bgResId = R.drawable.user_profile_image_7; break;
                case "portrait08": bgResId = R.drawable.user_profile_image_8; break;
                case "portrait09": bgResId = R.drawable.user_profile_image_9; break;
                case "portrait10": bgResId = R.drawable.user_profile_image_10; break;
                case "portrait11": bgResId = R.drawable.user_profile_image_11; break;
                case "portrait12": bgResId = R.drawable.user_profile_image_12; break;
                case "portrait13": bgResId = R.drawable.user_profile_image_13; break;
                case "portrait14": bgResId = R.drawable.user_profile_image_14; break;
                default: bgResId = R.drawable.user_profile_image_1;
            }
            return bgResId;
        }

        public Map<String, String> toMap() {
            HashMap<String, String> map = new HashMap<>();
            map.put("roomName", roomName);
            map.put("roomId", roomId);
            map.put("userId", userId);
            map.put("backgroundId", backgroundId);
            return map;
        }
    }

    public static class MessageInfo implements Serializable{
        public String userName;
        public String content;
        public @DrawableRes int giftIcon = View.NO_ID;

        public MessageInfo(String userName, String content) {
            this.userName = userName;
            this.content = content;
        }

        public MessageInfo(String userName, String content, int giftIcon) {
            this.userName = userName;
            this.content = content;
            this.giftIcon = giftIcon;
        }

    }

    public static class UserInfo {

    }

    public interface DataListCallback<T> {
        void onSuccess(List<T> dataList);
        void onFailed(Exception e);
    }
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onFailed(Exception e);
    }
}
