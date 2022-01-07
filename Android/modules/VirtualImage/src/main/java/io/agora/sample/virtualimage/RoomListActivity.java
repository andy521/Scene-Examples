package io.agora.sample.virtualimage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import io.agora.sample.virtualimage.manager.FUManager;
import io.agora.sample.virtualimage.manager.RoomManager;
import io.agora.sample.virtualimage.manager.RoomManager.RoomInfo;
import io.agora.sample.virtualimage.manager.RtcManager;
import io.agora.uiwidget.basic.TitleBar;
import io.agora.uiwidget.function.RoomListView;

public class RoomListActivity extends AppCompatActivity {
    private final String TAG = "RoomListActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.virtual_image_room_list_activity);

        RoomManager.getInstance().init(this, getString(R.string.virtual_image_agora_app_id), getString(R.string.virtual_image_agora_token));
        FUManager.getInstance().init(this);
        RtcManager.getInstance().init(this, getString(R.string.virtual_image_agora_app_id), null);

        RoomListView roomListView = findViewById(R.id.room_list_view);
        roomListView.setListAdapter(new RoomListView.AbsRoomListAdapter<RoomInfo>() {

            @Override
            protected void onItemUpdate(RoomListView.RoomListItemViewHolder holder, RoomInfo item) {
                holder.bgView.setBackgroundResource(item.getAndroidBgId());
                holder.participantsLayout.setVisibility(View.GONE);
                holder.roomName.setText(item.roomName);
                holder.itemView.setOnClickListener(v -> gotoAudiencePage(item));
            }

            @Override
            protected void onRefresh() {
                RoomManager.getInstance().getAllRooms(new RoomManager.DataListCallback<RoomInfo>() {
                    @Override
                    public void onSuccess(List<RoomInfo> dataList) {
                        runOnUiThread(() -> {
                            mDataList.clear();
                            mDataList.addAll(dataList);
                            notifyDataSetChanged();
                            triggerDataListUpdateRun();
                        });

                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "", e);
                        runOnUiThread(() -> {
                            triggerDataListUpdateRun();
                        });
                    }
                });
            }

            @Override
            protected void onLoadMore() {

            }
        });

        TitleBar titleBar = findViewById(R.id.title_bar);
        titleBar.setTitleName(getResources().getString(R.string.virtual_image_app_name), 0);
        titleBar.setBgDrawable(io.agora.uiwidget.R.drawable.title_bar_bg_colorful);
        titleBar.setUserIcon(false, 0, null);

        ImageView startLiveIv = findViewById(R.id.btn_start_live);
        startLiveIv.setOnClickListener(v -> gotoPreviewPage());
    }


    private void gotoPreviewPage() {
        startActivity(new Intent(RoomListActivity.this, PreviewActivity.class));
    }

    private void gotoAudiencePage(RoomInfo roomInfo) {
        Intent intent = new Intent(RoomListActivity.this, AudienceDetailActivity.class);
        intent.putExtra("roomInfo", roomInfo);
        startActivity(intent);
    }
}
