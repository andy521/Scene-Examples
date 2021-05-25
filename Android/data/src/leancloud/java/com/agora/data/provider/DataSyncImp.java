package com.agora.data.provider;

import android.content.Context;
import android.text.TextUtils;

import com.agora.data.Config;
import com.agora.data.R;
import com.agora.data.model.Room;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.leancloud.AVException;
import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.livequery.AVLiveQuery;
import cn.leancloud.livequery.AVLiveQueryEventHandler;
import cn.leancloud.livequery.AVLiveQuerySubscribeCallback;
import cn.leancloud.push.PushService;
import cn.leancloud.types.AVNull;
import io.agora.baselibrary.BuildConfig;
import io.reactivex.MaybeObserver;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class DataSyncImp implements ISyncManager {

    private Gson mGson = new Gson();

    public DataSyncImp(Context mContext) {
        if (BuildConfig.DEBUG) {
            AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
        } else {
            AVOSCloud.setLogLevel(AVLogger.Level.ERROR);
        }

        String appid = mContext.getString(R.string.leancloud_app_id);
        String appKey = mContext.getString(R.string.leancloud_app_key);
        String url = mContext.getString(R.string.leancloud_server_url);
        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appKey) || TextUtils.isEmpty(url)) {
            throw new NullPointerException("please check \"strings_config.xml\"");
        }

        AVOSCloud.initialize(mContext, appid, appKey, url);

        PushService.startIfRequired(mContext);
    }

    @Override
    public void createRoom(Room room, DocumentReference.DataCallback callback) {
        AVObject avObject = new AVObject("ROOM");
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull AVObject avObject) {
                callback.onSuccess(new AgoraObject(avObject));
            }

            @Override
            public void onError(@NotNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void getRooms(CollectionReference.DataCallback callback) {
        AVQuery<AVObject> avQuery = AVQuery.getQuery("ROOM");
        avQuery.findInBackground()
                .subscribe(new Observer<List<AVObject>>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull List<AVObject> avObjects) {
                        List<AgoraObject> list = new ArrayList<>();
                        for (AVObject avObject : avObjects) {
                            list.add(new AgoraObject(avObject));
                        }
                        callback.onSuccess(list);
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void get(DocumentReference reference) {
        if (reference instanceof RoomReference) {
            AVQuery<AVObject> query = AVQuery.getQuery("ROOM");
            query.include(Config.MEMBER_ANCHORID);
            query.getInBackground(reference.getId())
                    .firstElement()
                    .subscribe(new MaybeObserver<AVObject>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NotNull AVObject avObject) {
                            reference.onSuccess(new AgoraObject(avObject));
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            reference.onFail(-1, e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else {
            String roomId = reference.getParent().getParent().getId();
            AVObject avObjectRoom = AVObject.createWithoutData("ROOM", roomId);

            String collectionKey = reference.getParent().getKey();
            AVQuery<AVObject> avQuery = AVQuery.getQuery(collectionKey);
            avQuery.whereEqualTo("roomId", avObjectRoom);
            avQuery.getInBackground(reference.getId())
                    .firstElement()
                    .subscribe(new MaybeObserver<AVObject>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NotNull AVObject avObject) {
                            reference.onSuccess(new AgoraObject(avObject));
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            reference.onFail(-1, e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    @Override
    public void getList(CollectionReference reference) {
        String roomId = reference.getParent().getId();
        AVObject avObjectRoom = AVObject.createWithoutData("ROOM", roomId);

        AVQuery<AVObject> avQuery = AVQuery.getQuery(reference.getKey());
        avQuery.whereEqualTo("roomId", avObjectRoom);
        avQuery.findInBackground()
                .subscribe(new Observer<List<AVObject>>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull List<AVObject> avObjects) {
                        List<AgoraObject> list = new ArrayList<>();
                        for (AVObject avObject : avObjects) {
                            list.add(new AgoraObject(avObject));
                        }
                        reference.onSuccess(list);
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        reference.onFail(-1, e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void add(CollectionReference reference, Object object) {
        String collectionKey = reference.getKey();
        AVObject avObject = new AVObject(collectionKey);
//        avObject.put(Config.USER_NAME, user.getName());
//        avObject.put(Config.USER_AVATAR, user.getAvatar());
        avObject.saveInBackground()
                .subscribe(new Observer<AVObject>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull AVObject avObject) {

                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        reference.onFail(-1, e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Override
    public void delete(DocumentReference reference) {
        if (reference instanceof RoomReference) {
            AVObject avObject = AVObject.createWithoutData("ROOM", reference.getId());
            avObject.deleteInBackground()
                    .subscribe(new Observer<AVNull>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NotNull AVNull avNull) {
                            reference.onSuccess();
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            reference.onFail(-1, e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else {
            String roomId = reference.getParent().getParent().getId();
            AVObject avObjectRoom = AVObject.createWithoutData("ROOM", roomId);

            String collectionKey = reference.getParent().getKey();
            AVObject avObjectcollection = AVObject.createWithoutData(collectionKey, reference.getId());
            avObjectcollection.deleteInBackground()
                    .subscribe(new Observer<AVNull>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NotNull AVNull avNull) {
                            reference.onSuccess();
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            reference.onFail(-1, e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    @Override
    public void update(DocumentReference reference, String key, Object data) {
        if (reference instanceof RoomReference) {
            AVObject avObject = AVObject.createWithoutData("ROOM", reference.getId());
            avObject.put(key, data);
            avObject.saveInBackground()
                    .subscribe(new Observer<AVObject>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NotNull AVObject avObject) {
                            reference.onSuccess();
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            reference.onFail(-1, e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else {
            String roomId = reference.getParent().getParent().getId();
            AVObject avObjectRoom = AVObject.createWithoutData("ROOM", roomId);

            String collectionKey = reference.getParent().getKey();
            AVObject avObjectcollection = AVObject.createWithoutData(collectionKey, reference.getId());
            avObjectcollection.put(key, data);
            avObjectcollection.saveInBackground()
                    .subscribe(new Observer<AVObject>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NotNull AVObject avObject) {
                            reference.onSuccess();
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            reference.onFail(-1, e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    private HashMap<SyncManager.EventListener, AVLiveQuery> events = new HashMap<>();

    @Override
    public void subcribe(DocumentReference reference, SyncManager.EventListener listener) {
        if (reference instanceof RoomReference) {
            AVQuery<AVObject> query = AVQuery.getQuery(reference.getParent().getKey());
            AVLiveQuery avLiveQuery = AVLiveQuery.initWithQuery(query);
            avLiveQuery.setEventHandler(new AVLiveQueryEventHandler() {

                @Override
                public void onObjectCreated(AVObject avObject) {
                    super.onObjectCreated(avObject);
                    listener.onCreated(new AgoraObject(avObject));
                }

                @Override
                public void onObjectUpdated(AVObject avObject, List<String> updatedKeys) {
                    super.onObjectUpdated(avObject, updatedKeys);
                    listener.onUpdated(new AgoraObject(avObject));
                }

                @Override
                public void onObjectDeleted(String objectId) {
                    super.onObjectDeleted(objectId);
                    listener.onDeleted(objectId);
                }
            });

            events.put(listener, avLiveQuery);
            avLiveQuery.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
                @Override
                public void done(AVException e) {
                    if (null != e) {
                        listener.onSubscribeError(1);
                    } else {
                    }
                }
            });
        }
    }

    @Override
    public void unsubcribe(DocumentReference reference, SyncManager.EventListener listener) {
        if (events.get(listener) != null) {
            events.get(listener).unsubscribeInBackground(new AVLiveQuerySubscribeCallback() {
                @Override
                public void done(AVException e) {

                }
            });
        }
    }
}
