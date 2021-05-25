package com.agora.data.provider;

import com.agora.data.model.Room;

public interface ISyncManager {
    void createRoom(Room room, DocumentReference.DataCallback callback);

    void getRooms(CollectionReference.DataCallback callback);

    void get(DocumentReference reference);

    void getList(CollectionReference reference);

    void add(CollectionReference reference, Object object);

    void delete(DocumentReference reference);

    void update(DocumentReference reference, String key, Object data);

    void subcribe(DocumentReference reference, SyncManager.EventListener listener);

    void unsubcribe(DocumentReference reference, SyncManager.EventListener listener);
}