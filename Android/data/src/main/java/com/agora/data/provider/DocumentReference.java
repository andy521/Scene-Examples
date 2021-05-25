package com.agora.data.provider;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenhengfei(Aslanchen)
 * @date 2021/5/25
 */
public class DocumentReference {

    private String id;
    private HashMap<String, Object> whereEQ = new HashMap<>();

    private CollectionReference parent;
    private DataCallback callbackData;
    private Callback callback;

    public DocumentReference(CollectionReference parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public CollectionReference getParent() {
        return parent;
    }

    public DocumentReference whereEQ(String key, Object value) {
        whereEQ.put(key, value);
        return this;
    }

    public void get(DataCallback callback) {
        this.callbackData = callback;
        SyncManager.Instance().get(this);
    }

    public void set(Object data, Callback callback) {
        this.callback = callback;
//        SyncManager.Instance().s(this, key, data);
    }

    public void update(@NonNull Map<String, Object> data, Callback callback) {
        this.callback = callback;
    }

    public void update(String key, Object data, Callback callback) {
        this.callback = callback;
        SyncManager.Instance().update(this, key, data);
    }

    public void delete(Callback callback) {
        this.callback = callback;
        SyncManager.Instance().delete(this);
    }

    public void subcribe(SyncManager.EventListener listener) {
        SyncManager.Instance().subcribe(this, listener);
    }

    public void onSuccess(AgoraObject result) {
        this.callbackData.onSuccess(result);
    }

    public void onSuccess() {
        this.callback.onSuccess();
    }

    public void onFail(int code, String msg) {
        if (this.callback != null) {
            this.callback.onFail(code, msg);
        }

        if (this.callbackData != null) {
            this.callbackData.onFail(code, msg);
        }
    }

    public interface Callback {
        void onSuccess();

        void onFail(int code, String msg);
    }

    public interface DataCallback {
        void onSuccess(AgoraObject result);

        void onFail(int code, String msg);
    }
}
