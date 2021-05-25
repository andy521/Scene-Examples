package com.agora.data.provider;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * @author chenhengfei(Aslanchen)
 * @date 2021/5/25
 */
public class CollectionReference {

    private String key;

    private RoomReference parent;
    private DocumentReference mDocumentReference;
    private CollectionReference.DataCallback callback;

    public CollectionReference(RoomReference parent, String key) {
        this.parent = parent;
        this.key = key;
    }

    public RoomReference getParent() {
        return parent;
    }

    public String getKey() {
        return key;
    }

    public DocumentReference document(@NonNull String id) {
        mDocumentReference = new DocumentReference(this, id);
        return mDocumentReference;
    }

    public void add(Object data) {
        SyncManager.Instance().add(this, data);
    }

    public void get(CollectionReference.DataCallback callback) {
        this.callback = callback;
        SyncManager.Instance().getList(this);
    }

    public void onSuccess(List<AgoraObject> result) {
        this.callback.onSuccess(result);
    }

    public void onFail(int code, String msg) {
        this.callback.onFail(code, msg);
    }

    public interface DataCallback {
        void onSuccess(List<AgoraObject> result);

        void onFail(int code, String msg);
    }
}
