package io.agora.live.multihost;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class MultiHostActivity extends AppCompatActivity {

    private final static String EXTRA_ROOM_NAME = "RoomName";


    public static Intent launch(Context context, String roomName) {
        Intent intent = new Intent(context, MultiHostActivity.class);
        intent.putExtra(EXTRA_ROOM_NAME, roomName);
        return intent;
    }

    private final RtcManager rtcManager = new RtcManager();
    private volatile int publishLocalUid = -1;

    private final List<Integer> seatLayoutIdList = Arrays.asList(
            R.id.seat_01,
            R.id.seat_02,
            R.id.seat_03,
            R.id.seat_04,
            R.id.seat_05,
            R.id.seat_06,
            R.id.seat_07,
            R.id.seat_08,
            R.id.seat_09
    );

    private final View.OnClickListener seatClickListener = v -> {
        int id = v.getId();
        int index = seatLayoutIdList.indexOf(id);
        if (index < 0) {
            return;
        }
        publicVideo(index);
    };

    private void publicVideo(int seatIndex) {
        removeVideoView(publishLocalUid);
        int uid = genBroadcastUid(seatIndex);
        rtcManager.leaveChannel();
        rtcManager.setClientRole(true);
        rtcManager.joinChannel(getLocalChannelId(), uid);
        renderLocalVideo(uid);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_host);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(getLocalChannelId());
        initView();
        initManager();
        joinAudienceChannel();
    }

    private void initView() {
        for (Integer id : seatLayoutIdList) {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(seatClickListener);
            }
        }
    }

    private void initManager() {
        rtcManager.init(this, getAgoraAppId(), new RtcManager.OnInitializeListener() {
            @Override
            public void onError(int code, String message) {
                showToast(message, () -> finish());
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onUserJoined(int uid) {
                renderRemoteVideo(uid);
            }

            @Override
            public void onUserOffline(int uid) {
                removeVideoView(uid);
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid) {
                publishLocalUid = uid;
            }

        });
    }

    private void removeVideoView(int uid){
        int seatIndex = parseBroadcastUid(uid);
        runOnUiThread(() -> {
            if (seatIndex >= 0 && seatIndex < seatLayoutIdList.size()) {
                cleanSeatView(seatIndex, false);
            } else {
                Toast.makeText(MultiHostActivity.this, "Can not find seat index of uid " + uid, Toast.LENGTH_LONG).show();
            }
        });
    }

    private View cleanSeatView(int seatIndex, boolean keep) {
        FrameLayout videoView = findViewById(seatLayoutIdList.get(seatIndex));
        return rtcManager.cleanVideoView(videoView, genBroadcastUid(seatIndex), keep);
    }

    private void renderLocalVideo(int uid){
        int seatIndex = parseBroadcastUid(uid);
        runOnUiThread(() -> {
            if (seatIndex >= 0 && seatIndex < seatLayoutIdList.size()) {
                View view = cleanSeatView(seatIndex, false);
                rtcManager.renderLocalVideo(findViewById(seatLayoutIdList.get(seatIndex)), uid, view);
            } else {
                Toast.makeText(MultiHostActivity.this, "Can not find seat index of uid " + uid, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderRemoteVideo(int uid) {
        int seatIndex = parseBroadcastUid(uid);
        runOnUiThread(() -> {
            if (seatIndex >= 0 && seatIndex < seatLayoutIdList.size()) {
                View view = cleanSeatView(seatIndex, true);
                rtcManager.renderRemoteVideo(findViewById(seatLayoutIdList.get(seatIndex)), uid, view);
            } else {
                Toast.makeText(MultiHostActivity.this, "Can not find seat index of uid " + uid, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void joinAudienceChannel() {
        rtcManager.setClientRole(false);
        rtcManager.joinChannel(getLocalChannelId(), 0);
    }

    private void showToast(String message, Runnable after) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            Toast.makeText(MultiHostActivity.this, message, Toast.LENGTH_LONG).show();
            if (after != null) {
                after.run();
            }
            return;
        }
        if (!TextUtils.isEmpty(message)) {
            runOnUiThread(() -> {
                Toast.makeText(MultiHostActivity.this, message, Toast.LENGTH_LONG).show();
                if (after != null) {
                    after.run();
                }
            });
        }
    }

    private int genBroadcastUid(int index) {
        if(index < 0 || index >= seatLayoutIdList.size()){
            return -1;
        }
        return 10000 + index;
    }

    private int parseBroadcastUid(int uid) {
        int index = uid - 10000;
        if(index < 0 || index >= seatLayoutIdList.size()){
            return -1;
        }
        return index;
    }

    private String getLocalChannelId() {
        return getIntent().getStringExtra(EXTRA_ROOM_NAME);
    }

    private String getAgoraAppId() {
        String appId = getString(R.string.agora_app_id);
        if (TextUtils.isEmpty(appId)) {
            throw new RuntimeException("the app id is empty");
        }
        return appId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcManager.release();
    }

    public interface DataRunnable<T>{
        boolean run(T data);
    }
}
