package io.agora.superapp.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.agora.baselibrary.base.DataBindBaseActivity;
import io.agora.superapp.Constants;
import io.agora.superapp.R;
import io.agora.superapp.databinding.RoomListActivityBinding;
import io.agora.superapp.model.RoomInfo;
import io.agora.superapp.util.DataListCallback;
import io.agora.superapp.util.PreferenceUtil;
import io.agora.superapp.widget.SpaceItemDecoration;
import io.agora.syncmanager.rtm.IObject;
import io.agora.syncmanager.rtm.SyncManager;
import io.agora.syncmanager.rtm.SyncManagerException;
import pub.devrel.easypermissions.EasyPermissions;

import static io.agora.superapp.Constants.SYNC_DEFAULT_CHANNEL;

public class RoomListActivity extends DataBindBaseActivity<RoomListActivityBinding> {
    private static final String TAG = RoomListActivity.class.getSimpleName();
    private static final int RECYCLER_VIEW_SPAN_COUNT = 2;
    private static final int TAG_PERMISSTION_REQUESTCODE = 1000;
    private static final String[] PERMISSTION = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    private RoomListAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.room_list_activity;
    }

    @Override
    protected void iniBundle(@NonNull Bundle bundle) {
        // do nothing
    }

    @Override
    protected void iniView() {
        mDataBinding.hostInSwipe.setNestedScrollingEnabled(false);
        mDataBinding.hostInRoomListRecycler.setVisibility(View.VISIBLE);
        mDataBinding.hostInRoomListRecycler.setLayoutManager(new GridLayoutManager(this, RECYCLER_VIEW_SPAN_COUNT));
        mAdapter = new RoomListAdapter();
        mDataBinding.hostInRoomListRecycler.setAdapter(mAdapter);
        mDataBinding.hostInRoomListRecycler.addItemDecoration(new SpaceItemDecoration(getResources()
                .getDimensionPixelSize(R.dimen.activity_horizontal_margin), RECYCLER_VIEW_SPAN_COUNT));
    }

    @Override
    protected void iniListener() {
        mDataBinding.hostInSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadRoomList(data -> {
                    runOnUiThread(() -> {
                        mAdapter.appendList(data, true);
                        checkNoData();
                        mDataBinding.hostInSwipe.setRefreshing(false);
                    });
                });
            }
        });
        mDataBinding.liveRoomStartBroadcast.setOnClickListener(v -> {
            alertCreateDialog();
        });
        mAdapter.setItemClickListener(new RoomListAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(RoomInfo item) {
                SyncManager.Instance().joinScene(item.roomId);
                SyncManager.Instance()
                        .getScene(item.roomId)
                        .collection(Constants.SYNC_COLLECTION_ROOM_INFO)
                        .get(new SyncManager.DataListCallback() {
                            @Override
                            public void onSuccess(List<IObject> result) {
                                if(result == null || result.size() <= 0){
                                    Toast.makeText(RoomListActivity.this, "Room has been destroy", Toast.LENGTH_SHORT).show();
                                    deleteRoom(item, () -> mAdapter.remoteItem(item.roomId));
                                    return;
                                }
                                IObject iObject = result.get(result.size() - 1);
                                RoomInfo roomInfo = iObject.toObject(RoomInfo.class);
                                roomInfo.objectId = iObject.getId();
                                if (EasyPermissions.hasPermissions(RoomListActivity.this, PERMISSTION)) {
                                    startActivity(AudienceActivity.launch(RoomListActivity.this, roomInfo));
                                } else {
                                    EasyPermissions.requestPermissions(RoomListActivity.this, getString(R.string.error_leak_permission),
                                            TAG_PERMISSTION_REQUESTCODE, PERMISSTION);
                                }
                            }

                            @Override
                            public void onFail(SyncManagerException exception) {
                                Toast.makeText(RoomListActivity.this, "Room has been destroy", Toast.LENGTH_SHORT).show();
                                deleteRoom(item, () -> mAdapter.remoteItem(item.roomId));
                            }
                        });
            }

            @Override
            public void onItemDeleteClicked(RoomInfo item, int position) {
                new AlertDialog.Builder(RoomListActivity.this)
                        .setTitle(R.string.cmm_tip)
                        .setMessage("Sure to delete the room?")
                        .setPositiveButton(R.string.cmm_ok, (dialog, which) -> deleteRoom(item, () -> {
                            mAdapter.remoteItem(item.roomId);
                        }))
                        .setNegativeButton(R.string.cmm_cancel, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
        mDataBinding.ivHead.setOnClickListener(v -> {
            startActivity(new Intent(RoomListActivity.this, UserProfileActivity.class));
        });
    }

    @Override
    protected void iniData() {
        PreferenceUtil.init(getApplicationContext());
        initSyncManager();
        mDataBinding.hostInSwipe.setRefreshing(true);
        loadRoomList(data -> {
            runOnUiThread(() -> {
                Log.d(TAG, "initData loadRoomList data=" + data);
                mAdapter.appendList(data, true);
                checkNoData();
                mDataBinding.hostInSwipe.setRefreshing(false);
            });
        });
    }

    private void checkNoData() {
        boolean hasData = mAdapter.getItemCount() > 0;
        mDataBinding.noDataBg.setVisibility(hasData? View.GONE: View.VISIBLE);
    }

    private void alertCreateDialog() {
        if (EasyPermissions.hasPermissions(this, PERMISSTION)) {
            startActivity(new Intent(this, PreviewActivity.class));
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.error_leak_permission),
                    TAG_PERMISSTION_REQUESTCODE, PERMISSTION);
        }
    }

    // ====== business method ======

    private void initSyncManager(){
        HashMap<String, String> params = new HashMap<>();
        params.put("appid", getString(R.string.agora_app_id));
        params.put("defaultChannel", SYNC_DEFAULT_CHANNEL);
        SyncManager.Instance().init(this, params);
    }

    private void loadRoomList(DataListCallback<RoomInfo> callback){
        SyncManager.Instance().getScenes(new SyncManager.DataListCallback() {
            @Override
            public void onSuccess(List<IObject> result) {
                List<RoomInfo> list = new ArrayList<>();
                for (IObject item : result) {
                    RoomInfo roomInfo = item.toObject(RoomInfo.class);
                    roomInfo.objectId = item.getId();
                    list.add(roomInfo);
                }
                Collections.sort(list, (o1, o2) -> (int) (o2.createTime - o1.createTime));
                callback.onSuccess(list);
            }

            @Override
            public void onFail(SyncManagerException exception) {
                Log.e(this.getClass().getSimpleName(), "loadRoomList error: " + exception.toString());
                callback.onSuccess(null);
            }
        });

    }

    private void deleteRoom(RoomInfo roomInfo, Runnable successRun) {
        SyncManager.Instance()
                .getScene(roomInfo.roomId)
                .delete(new SyncManager.Callback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(successRun);
                    }

                    @Override
                    public void onFail(SyncManagerException exception) {
                        runOnUiThread(() -> Toast.makeText(RoomListActivity.this, "deleteRoomInfo failed exception: " + exception.toString(), Toast.LENGTH_LONG).show());

                    }
                });
    }



}
