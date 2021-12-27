package io.agora.superapp.view;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.agora.example.base.BaseActivity;
import io.agora.superapp.R;
import io.agora.superapp.databinding.SuperappRoomListActivityBinding;
import io.agora.superapp.model.RoomInfo;
import io.agora.superapp.util.DataListCallback;
import io.agora.superapp.util.PreferenceUtil;
import io.agora.superapp.widget.SpaceItemDecoration;
import io.agora.syncmanager.rtm.IObject;
import io.agora.syncmanager.rtm.Sync;
import io.agora.syncmanager.rtm.SyncManagerException;

public class RoomListActivity extends BaseActivity<SuperappRoomListActivityBinding> {
    private static final String TAG = RoomListActivity.class.getSimpleName();
    private static final int RECYCLER_VIEW_SPAN_COUNT = 2;
    private static final String[] PERMISSTION = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    private RoomListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.hostInSwipe.setNestedScrollingEnabled(false);
        mBinding.hostInRoomListRecycler.setVisibility(View.VISIBLE);
        mBinding.hostInRoomListRecycler.setLayoutManager(new GridLayoutManager(this, RECYCLER_VIEW_SPAN_COUNT));
        mAdapter = new RoomListAdapter();
        mBinding.hostInRoomListRecycler.setAdapter(mAdapter);
        mBinding.hostInRoomListRecycler.addItemDecoration(new SpaceItemDecoration(getResources()
                .getDimensionPixelSize(R.dimen.activity_horizontal_margin), RECYCLER_VIEW_SPAN_COUNT));

        mBinding.hostInSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadRoomList(data -> {
                    runOnUiThread(() -> {
                        mAdapter.appendList(data, true);
                        checkNoData();
                        mBinding.hostInSwipe.setRefreshing(false);
                    });
                });
            }
        });
        mBinding.liveRoomStartBroadcast.setOnClickListener(v -> {
            AndPermission.with(RoomListActivity.this)
                    .runtime()
                    .permission(PERMISSTION)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            startActivity(new Intent(RoomListActivity.this, PreviewActivity.class));
                        }
                    })
                    .start();
        });
        mAdapter.setItemClickListener(new RoomListAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(RoomInfo item) {
                AndPermission.with(RoomListActivity.this)
                        .runtime()
                        .permission(PERMISSTION)
                        .onGranted(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                startActivity(AudienceActivity.launch(RoomListActivity.this, item));
                            }
                        })
                        .start();
            }
        });
        mBinding.ivHead.setOnClickListener(v -> {
            startActivity(new Intent(RoomListActivity.this, UserProfileActivity.class));
        });

        PreferenceUtil.init(getApplicationContext());
        initSyncManager();
        mBinding.hostInSwipe.setRefreshing(true);
        loadRoomList(data -> {
            runOnUiThread(() -> {
                Log.d(TAG, "initData loadRoomList data=" + data);
                mAdapter.appendList(data, true);
                checkNoData();
                mBinding.hostInSwipe.setRefreshing(false);
            });
        });
    }

    private void checkNoData() {
        boolean hasData = mAdapter.getItemCount() > 0;
        mBinding.noDataBg.setVisibility(hasData? View.GONE: View.VISIBLE);
    }

    // ====== business method ======


    private void initSyncManager(){
        HashMap<String, String> params = new HashMap<>();
        params.put("appid", getString(R.string.agora_app_id));
        params.put("defaultChannel", "SuperApp");
        Sync.Instance().init(this, params, new Sync.Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail(SyncManagerException exception) {

            }
        });
    }

    private void loadRoomList(DataListCallback<RoomInfo> callback){
        Sync.Instance().getScenes(new Sync.DataListCallback() {
            @Override
            public void onSuccess(List<IObject> result) {
                List<RoomInfo> list = new ArrayList<>();
                for (IObject item : result) {
                    list.add(item.toObject(RoomInfo.class));
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

}
