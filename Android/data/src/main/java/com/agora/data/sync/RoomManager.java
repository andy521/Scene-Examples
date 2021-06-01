package com.agora.data.sync;

import android.content.Context;

import androidx.annotation.NonNull;

import com.agora.data.BaseError;
import com.agora.data.model.AgoraMember;
import com.agora.data.model.BusinessMember;
import com.agora.data.model.BusinessRoom;
import com.agora.data.model.Member;
import com.agora.data.model.Room;
import com.agora.data.observer.DataObserver;
import com.agora.data.provider.AgoraObject;

/**
 * 房间控制
 *
 * @author chenhengfei(Aslanchen)
 * @date 2021/06/01
 */
public final class RoomManager {

    private volatile static RoomManager instance;

    private Context mContext;

    private RoomManager(Context mContext) {
        this.mContext = mContext;
        SyncManager.Instance().init(mContext);
    }

    public static RoomManager Instance(Context mContext) {
        if (instance == null) {
            synchronized (RoomManager.class) {
                if (instance == null)
                    instance = new RoomManager(mContext.getApplicationContext());
            }
        }
        return instance;
    }

    public void createRoom(BusinessRoom room, DataCallback<Room> callback) {
//        DataRepositroy.Instance(mContext)
//                .creatRoom(room)
//                .subscribe(new DataObserver<Room>(mContext) {
//                    @Override
//                    public void handleError(@NonNull BaseError e) {
//                        callback.onFail(e.getCode(), e.getMessage());
//                    }
//
//                    @Override
//                    public void handleSuccess(@NonNull Room room) {
//                        callback.onSuccess(room);
//                    }
//                });
    }

    public void joinRoom(BusinessRoom room, BusinessMember member, DataCallback<Room> callback) {
        //1：判断房间是否存在
        SyncManager.Instance()
                .getRoom(room.getObjectId())
                .get(new SyncManager.DataItemCallback() {
                    @Override
                    public void onSuccess(AgoraObject result) {
                        //2：删除一次，因为有可能是异常退出导致第二次进入，所以删除之前的。
                        SyncManager.Instance()
                                .getRoom(room.getObjectId())
                                .collection(AgoraMember.TABLE_NAME)
                                .query(new Query()
                                        .whereEqualTo(AgoraMember.COLUMN_USERID, member.getUser().getObjectId()))
                                .delete(new SyncManager.Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onFail(int code, String msg) {

                                    }
                                });

                        AgoraMember agoraMember = new AgoraMember();
                        SyncManager.Instance()
                                .getRoom(room.getObjectId())
                                .collection(AgoraMember.TABLE_NAME)
                                .add(agoraMember.toHashMap(), new SyncManager.DataItemCallback() {
                                    @Override
                                    public void onSuccess(AgoraObject result) {

                                    }

                                    @Override
                                    public void onFail(int code, String msg) {

                                    }
                                });
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        callback.onFail(code, msg);
                    }
                });
    }

    public void leaveRoom(Room room) {
        SyncManager.Instance()
                .getRoom(room.getObjectId())
                .delete(new SyncManager.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFail(int code, String msg) {

                    }
                });
    }

    public void toggleSelfAudio(Room room, Member member, boolean isMute) {
        SyncManager.Instance()
                .getRoom(room.getObjectId())
                .collection(AgoraMember.TABLE_NAME)
                .document(member.getObjectId())
                .update(AgoraMember.COLUMN_ISSELFAUDIOMUTED, isMute, new SyncManager.DataItemCallback() {
                    @Override
                    public void onSuccess(AgoraObject result) {
                        AgoraMember memberNew = result.toObject(AgoraMember.class);
                    }

                    @Override
                    public void onFail(int code, String msg) {

                    }
                });
    }

    public void toggleAudio(Room room, Member member, boolean isMute) {
        SyncManager.Instance()
                .getRoom(room.getObjectId())
                .collection(AgoraMember.TABLE_NAME)
                .document(member.getObjectId())
                .update(AgoraMember.COLUMN_ISAUDIOMUTED, isMute, new SyncManager.DataItemCallback() {
                    @Override
                    public void onSuccess(AgoraObject result) {
                        AgoraMember memberNew = result.toObject(AgoraMember.class);
                    }

                    @Override
                    public void onFail(int code, String msg) {

                    }
                });
    }

    public void changeRole(Room room, Member member, int role) {
        SyncManager.Instance()
                .getRoom(room.getObjectId())
                .collection(AgoraMember.TABLE_NAME)
                .document(member.getObjectId())
                .update(AgoraMember.COLUMN_ROLE, role, new SyncManager.DataItemCallback() {
                    @Override
                    public void onSuccess(AgoraObject result) {
                        AgoraMember memberNew = result.toObject(AgoraMember.class);
                    }

                    @Override
                    public void onFail(int code, String msg) {

                    }
                });
    }

    public interface DataCallback<T> {
        void onSuccess(T result);

        void onFail(int code, String msg);
    }
}
