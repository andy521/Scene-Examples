package com.agora.data.sync;

import java.util.HashMap;

public interface ISyncManager {
    void get(DocumentReference reference, SyncManager.DataItemCallback callback);

    void getList(CollectionReference reference, SyncManager.DataListCallback callback);

    void add(CollectionReference reference, HashMap<String, Object> datas, SyncManager.DataItemCallback callback);

    void delete(DocumentReference reference, SyncManager.Callback callback);

    void deleteBatch(CollectionReference reference, SyncManager.Callback callback);

    void update(DocumentReference reference, String key, Object data, SyncManager.DataItemCallback callback);

    void subcribe(DocumentReference reference, SyncManager.EventListener listener);

    void unsubcribe(DocumentReference reference, SyncManager.EventListener listener);
}