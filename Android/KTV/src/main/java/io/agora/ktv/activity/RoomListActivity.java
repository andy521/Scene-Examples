package io.agora.ktv.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agora.data.BaseError;
import com.agora.data.SimpleRoomEventCallback;
import com.agora.data.manager.RoomManager;
import com.agora.data.manager.UserManager;
import com.agora.data.model.AgoraRoom;
import com.agora.data.model.Room;
import com.agora.data.model.User;
import com.agora.data.observer.DataObserver;
import com.agora.data.sync.SyncManager;

import java.util.List;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.baselibrary.base.OnItemClickListener;
import io.agora.baselibrary.util.ToastUtile;
import io.agora.ktv.R;
import io.agora.ktv.adapter.RoomListAdapter;
import io.agora.ktv.databinding.KtvActivityRoomListBinding;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 房间列表
 *
 * @author chenhengfei@agora.io
 */
public class RoomListActivity extends DataBindBaseActivity<KtvActivityRoomListBinding> implements View.OnClickListener,
        OnItemClickListener<AgoraRoom>, EasyPermissions.PermissionCallbacks, SwipeRefreshLayout.OnRefreshListener {

    private static final int TAG_PERMISSTION_REQUESTCODE = 1000;
    private static final String[] PERMISSTION = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    private RoomListAdapter mAdapter;

    private SimpleRoomEventCallback mSimpleRoomEventCallback = new SimpleRoomEventCallback() {
        @Override
        public void onRoomClosed(@NonNull Room room, boolean fromUser) {
            super.onRoomClosed(room, fromUser);

//            mAdapter.deleteItem(room);
            mDataBinding.tvEmpty.setVisibility(mAdapter.getItemCount() <= 0 ? View.VISIBLE : View.GONE);
        }
    };

    @Override
    protected void iniBundle(@NonNull Bundle bundle) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.ktv_activity_room_list;
    }

    @Override
    protected void iniView() {
        mAdapter = new RoomListAdapter(null, this);
        mDataBinding.list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mDataBinding.list.setAdapter(mAdapter);
//        mDataBinding.list.addItemDecoration(new SpaceItemDecoration(this));
    }

    @Override
    protected void iniListener() {
        RoomManager.Instance(this).addRoomEventCallback(mSimpleRoomEventCallback);
        mDataBinding.swipeRefreshLayout.setOnRefreshListener(this);
        mDataBinding.ivHead.setOnClickListener(this);
        mDataBinding.btCrateRoom.setOnClickListener(this);
    }

    @Override
    protected void iniData() {
        UserManager.Instance(this).setupDataRepositroy(io.agora.ktv.data.DataRepositroy.Instance(this));

        mDataBinding.btCrateRoom.setVisibility(View.VISIBLE);
        mDataBinding.tvEmpty.setVisibility(mAdapter.getItemCount() <= 0 ? View.VISIBLE : View.GONE);

        login();
    }

    private void login() {
        UserManager.Instance(this)
                .loginIn()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mLifecycleProvider.bindToLifecycle())
                .subscribe(new DataObserver<User>(this) {
                    @Override
                    public void handleError(@NonNull BaseError e) {

                    }

                    @Override
                    public void handleSuccess(@NonNull User user) {
                        mDataBinding.swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                mDataBinding.swipeRefreshLayout.setRefreshing(true);
                                loadRooms();
                            }
                        });
                    }
                });
    }

    private void loadRooms() {
        mAdapter.clear();

        SyncManager.Instance()
                .getRooms()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mLifecycleProvider.bindToLifecycle())
                .subscribe(new Observer<List<AgoraRoom>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<AgoraRoom> agoraRooms) {
                        mAdapter.setDatas(agoraRooms);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mDataBinding.swipeRefreshLayout.setRefreshing(false);
                        ToastUtile.toastShort(RoomListActivity.this, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mDataBinding.swipeRefreshLayout.setRefreshing(false);
                        mDataBinding.tvEmpty.setVisibility(mAdapter.getItemCount() <= 0 ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (!UserManager.Instance(this).isLogin()) {
            login();
            return;
        }

        if (v.getId() == R.id.btCrateRoom) {
            gotoCreateRoom();
        }
    }

    private void gotoCreateRoom() {
        User mUser = UserManager.Instance(this).getUserLiveData().getValue();
        if (mUser == null) {
            return;
        }

        AgoraRoom mRoom = new AgoraRoom();
        mRoom.setName("Test");
        mRoom.setOwnerId(mUser.getObjectId());

        SyncManager.Instance()
                .creatRoom(mRoom)
                .subscribe(new Observer<AgoraRoom>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull AgoraRoom agoraRoom) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
//        if (EasyPermissions.hasPermissions(this, PERMISSTION)) {
//            new CreateRoomDialog().show(getSupportFragmentManager(), new CreateRoomDialog.ICreateCallback() {
//                @Override
//                public void onRoomCreated(@NonNull Room room) {
//                    Intent intent = ChatRoomActivity.newIntent(RoomListActivity.this, room);
//                    startActivity(intent);
//                }
//            });
//        } else {
//            EasyPermissions.requestPermissions(this, getString(R.string.error_permisstion),
//                    TAG_PERMISSTION_REQUESTCODE, PERMISSTION);
//        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onItemClick(@NonNull AgoraRoom data, View view, int position, long id) {
//        if (!EasyPermissions.hasPermissions(this, PERMISSTION)) {
//            EasyPermissions.requestPermissions(this, getString(R.string.error_permisstion),
//                    TAG_PERMISSTION_REQUESTCODE, PERMISSTION);
//            return;
//        }
//
//        Room roomCur = RoomManager.Instance(this).getRoom();
//        if (roomCur != null) {
//            if (!ObjectsCompat.equals(roomCur, data)) {
//                ToastUtile.toastShort(this, R.string.error_joined_room);
//                return;
//            }
//        }

        Intent intent = RoomActivity.newIntent(this, data);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        RoomManager.Instance(this).removeRoomEventCallback(mSimpleRoomEventCallback);
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        loadRooms();
    }
}
