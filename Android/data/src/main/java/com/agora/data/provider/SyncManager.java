package com.agora.data.provider;

import com.agora.data.model.Room;

public final class SyncManager implements ISyncManager {

    private volatile static SyncManager instance;

    private SyncManager() {
    }

    public static SyncManager Instance() {
        if (instance == null) {
            synchronized (SyncManager.class) {
                if (instance == null)
                    instance = new SyncManager();
            }
        }
        return instance;
    }

    private ISyncManager mISyncManager;

    public RoomReference getRoom(String id) {
        return new RoomReference(id);
    }

    @Override
    public void createRoom(Room room, DocumentReference.DataCallback callback) {
        mISyncManager.createRoom(room, callback);
    }

    @Override
    public void getRooms(CollectionReference.DataCallback callback) {
        mISyncManager.getRooms(callback);
    }

    @Override
    public void get(DocumentReference reference) {
        mISyncManager.get(reference);
    }

    @Override
    public void getList(CollectionReference reference) {
        mISyncManager.getList(reference);
    }

    @Override
    public void add(CollectionReference reference, Object object) {
        mISyncManager.add(reference, object);
    }

    @Override
    public void delete(DocumentReference reference) {
        mISyncManager.delete(reference);
    }

    @Override
    public void update(DocumentReference reference, String key, Object data) {
        mISyncManager.update(reference, key, data);
    }

    @Override
    public void subcribe(DocumentReference reference, SyncManager.EventListener listener) {
        mISyncManager.subcribe(reference, listener);
    }

    @Override
    public void unsubcribe(DocumentReference reference, SyncManager.EventListener listener) {
        mISyncManager.unsubcribe(reference, listener);
    }

    public interface EventListener {
        void onCreated(AgoraObject item);

        void onUpdated(AgoraObject item);

        void onDeleted(String objectId);

        void onSubscribeError(int error);
    }
}
