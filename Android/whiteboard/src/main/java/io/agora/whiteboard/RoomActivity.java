package io.agora.whiteboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;
import com.google.gson.Gson;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.CommonCallbacks;
import com.herewhite.sdk.Converter;
import com.herewhite.sdk.ConverterCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.AnimationMode;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.ContentModeConfig;
import com.herewhite.sdk.domain.ConversionInfo;
import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.ConvertedFiles;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Point;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RectangleConfig;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.UrlInterrupter;
import com.herewhite.sdk.domain.ViewMode;
import com.herewhite.sdk.domain.WhiteDisplayerState;
import com.herewhite.whiteboard.R;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import io.agora.baselibrary.util.Utils;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.models.ClientRoleOptions;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import wendu.dsbridge.DWebView;

import static io.agora.rtc.Constants.RENDER_MODE_HIDDEN;

public class RoomActivity extends AppCompatActivity {

    private static final String TAG = RoomActivity.class.getSimpleName();

    final String EVENT_NAME = "WhiteCommandCustomEvent";

    final String SCENE_DIR = "/dir";
    final String ROOM_INFO = "room info";
    final String ROOM_ACTION = "room action";
    final Gson gson = new Gson();
    final DemoAPI demoAPI = new DemoAPI();

    private String uuid;
    private String roomToken;
    private boolean isHost = false;
    private String channel;
    private RtcEngine rtcEngine;
    private FrameLayout videoPod;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int dataStreamId;

    WhiteboardView whiteboardView;
    Room room;

    private void initEngine(){
        try {
            rtcEngine = RtcEngine.create(getApplicationContext(), getString(R.string.app_id), iRtcEngineEventHandler);

            rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            rtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY, Constants.AUDIO_SCENARIO_CHATROOM_ENTERTAINMENT);
            rtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration());
            rtcEngine.enableVideo();
            if(isHost){
                dataStreamId = rtcEngine.createDataStream(true, true);
                rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
                // Create render view by RtcEngine
                SurfaceView surfaceView = RtcEngine.CreateRendererView(RoomActivity.this);
                if(videoPod.getChildCount() > 0)
                {
                    videoPod.removeAllViews();
                }
                // Add to the local container
                videoPod.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                // Setup local video to render your local camera preview
                rtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, RENDER_MODE_HIDDEN, 0));
            }
            else{
                ClientRoleOptions clientRoleOptions = new ClientRoleOptions();
                clientRoleOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY;
                rtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE, clientRoleOptions);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }



    class MyGlobalState extends GlobalState {
        public String getOne() {
            return one;
        }

        public void setOne(String one) {
            this.one = one;
        }

        String one;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        whiteboardView.removeAllViews();
        whiteboardView.destroy();
        /**leaveChannel and Destroy the RtcEngine instance*/
        if(rtcEngine != null)
        {
            rtcEngine.leaveChannel();
            rtcEngine.stopPreview();
        }
        mHandler.post(RtcEngine::destroy);
    }

    private IRtcEngineEventHandler iRtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
//                ToastUtile.toastShort(RoomActivity.this, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid));
            if(isHost){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        long now = System.currentTimeMillis();
                        Log.i(TAG, "sendStreamMessage timestamp: " + now);
                        rtcEngine.sendStreamMessage(dataStreamId, String.valueOf(now).getBytes(Charset.forName("UTF-8")));
                        mHandler.postDelayed(this, 2000);
                    }
                });
            }
        }

        @Override
        public void onStreamMessage(int uid, int streamId, byte[] data) {
            long timeStamp = Utils.BytesToLong(data);
            Log.i(TAG, "onStreamMessage " + timeStamp);
            if(room != null){
                room.syncBlockTimestamp(timeStamp);
            }
        }

        @Override
        public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached) {
            Log.e(TAG, "onStreamMessageError:" + error);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
//                ToastUtile.toastShort(RoomActivity.this, String.format("user %d joined!", uid));

            if(videoPod.getChildCount() > 0){
                return;
            }
            else{
                mHandler.post(() ->
                {
                    // Create render view by RtcEngine
                    SurfaceView surfaceView = RtcEngine.CreateRendererView(RoomActivity.this);
                    // Add to the local container
                    videoPod.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    /**Display remote video stream*/
                    // Create render view by RtcEngine
                    surfaceView.setZOrderMediaOverlay(true);
                    // Setup remote video to render
                    rtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, RENDER_MODE_HIDDEN, uid));
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        whiteboardView = findViewById(R.id.white);
        DWebView.setWebContentsDebuggingEnabled(true);
        whiteboardView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        useHttpDnsService(false);

        LocalFileWebViewClient client = new LocalFileWebViewClient();
        client.setPptDirectory(getCacheDir().getAbsolutePath());
        whiteboardView.setWebViewClient(client);

        Intent intent = getIntent();
        String role = intent.getStringExtra(StartActivity.ROLE);
        isHost = StartActivity.HOST.equals(role);
        channel = intent.getStringExtra(StartActivity.CHANNEL_NAME);

        if (isHost) {
            createRoom(channel);
        } else {
            getRoomToken();
        }

        videoPod = findViewById(R.id.video_pod);
        initEngine();
        rtcEngine.joinChannel("", channel, null, 0);
    }

    //region room
    private void createRoom(String name) {
        demoAPI.getNewRoom(name, new DemoAPI.Result() {
            @Override
            public void success(String uuid, String roomToken) {
                joinRoom(uuid, roomToken);
            }

            @Override
            public void fail(String message) {
                alert("创建房间失败", message);
            }
        });
    }

    private void getRoomToken() {
        String uuid = "" + (int)(Math.random() * 1000);
        demoAPI.getRoomToken(demoAPI.getDemoUUID(), new DemoAPI.Result() {
            @Override
            public void success(String uuid, String roomToken) {
                joinRoom(uuid, roomToken);
            }

            @Override
            public void fail(String message) {
                alert("获取房间 token 失败", message);
            }
        });
    }

    private void joinRoom(String uuid, String roomToken) {
        logRoomInfo("room uuid: " + uuid + "\nroomToken: " + roomToken);

        this.uuid = uuid;
        this.roomToken = roomToken;

        WhiteSdkConfiguration sdkConfiguration = new WhiteSdkConfiguration(demoAPI.getAppIdentifier(), true);

        sdkConfiguration.setUserCursor(true);

        HashMap<String, String> map = new HashMap<>();
        map.put("宋体","https://your-cdn.com/Songti.ttf");
        sdkConfiguration.setFonts(map);

        WhiteSdk whiteSdk = new WhiteSdk(whiteboardView, RoomActivity.this, sdkConfiguration,
                new UrlInterrupter() {
                    @Override
                    public String urlInterrupter(String sourceUrl) {
                        return sourceUrl;
                    }
                });

        WhiteDisplayerState.setCustomGlobalStateClass(MyGlobalState.class);

        whiteSdk.setCommonCallbacks(new CommonCallbacks() {
            @Override
            public String urlInterrupter(String sourceUrl) {
                return sourceUrl;
            }

            @Override
            public void sdkSetupFail(SDKError error) {
                Log.e("ROOM_ERROR", error.toString());
            }

            @Override
            public void throwError(Object args) {

            }

            @Override
            public void onPPTMediaPlay() {
                logAction();
            }

            @Override
            public void onPPTMediaPause() {
                logAction();
            }

            @Override
            public void onMessage(JSONObject object) {

            }
        });

        RoomParams roomParams = new RoomParams(uuid, roomToken);

        final Date joinDate = new Date();
        logRoomInfo("native join " + joinDate);
        whiteSdk.joinRoom(roomParams, new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                showToast(phase.name());
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                logRoomInfo(gson.toJson(modifyState));
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room wRoom) {
                logRoomInfo("native join in room duration: " + (new Date().getTime() - joinDate.getTime()) / 1000f + "s");
                room = wRoom;
                addCustomEventListener();
            }

            @Override
            public void catchEx(SDKError t) {
                showToast(t.getMessage());
            }
        });
    }
    //endregion

    //region private
    private void alert(final String title, final String detail) {

        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(RoomActivity.this).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(detail);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    private void useHttpDnsService(boolean use) {
        if (use) {
            HttpDnsService httpDnsService = HttpDns.getService(getApplicationContext(), "188301");
            httpDnsService.setPreResolveHosts(new ArrayList<>(Arrays.asList("expresscloudharestoragev2.herewhite.com", "cloudharev2.herewhite.com", "scdncloudharestoragev3.herewhite.com", "cloudcapiv4.herewhite.com")));
            whiteboardView.setWebViewClient(new WhiteWebViewClient(httpDnsService));
        }
    }

    private void addCustomEventListener() {
        room.addMagixEventListener(EVENT_NAME, new EventListener() {
            @Override
            public void onEvent(EventEntry eventEntry) {
                logRoomInfo("customEvent payload: " + eventEntry.getPayload().toString());
                showToast(gson.toJson(eventEntry.getPayload()));
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        logRoomInfo( "width:" + whiteboardView.getWidth() / getResources().getDisplayMetrics().density + " height: " + whiteboardView.getHeight() / getResources().getDisplayMetrics().density);
        // onConfigurationChanged 调用时，横竖屏切换并没有完成，需要延迟调用
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                room.refreshViewSize();
                logRoomInfo( "width:" + whiteboardView.getWidth() / getResources().getDisplayMetrics().density + " height: " + whiteboardView.getHeight() / getResources().getDisplayMetrics().density);
            }
        }, 1000);
    }

    //endregion

    //region menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_command, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    private CameraBound customBound(double maxScale) {
        CameraBound bound = new CameraBound();
        bound.setCenterX(0d);
        bound.setCenterY(0d);
        bound.setHeight((double) (whiteboardView.getHeight() / this.getResources().getDisplayMetrics().density));
        bound.setWidth((double) (whiteboardView.getWidth() / this.getResources().getDisplayMetrics().density));
        ContentModeConfig contentModeConfig = new ContentModeConfig();
        contentModeConfig.setScale(maxScale);
        contentModeConfig.setMode(ContentModeConfig.ScaleMode.CENTER_INSIDE_SCALE);
        bound.setMaxContentMode(contentModeConfig);
        return bound;
    }

    public void scalePptToFit(MenuItem item) {
        room.scalePptToFit(AnimationMode.Continuous);
    }

    public void reconnect(MenuItem item) {
        final RoomActivity that = this;
        room.disconnect(new Promise<Object>() {
            @Override
            public void then(Object b) {
                joinRoom(that.uuid, that.roomToken);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    public void setWritableFalse(MenuItem item) {
        room.setWritable(false, new Promise<Boolean>() {
            @Override
            public void then(Boolean aBoolean) {
                logRoomInfo("room writable: " + aBoolean);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    public void setWritableTrue(MenuItem item) {
        room.setWritable(true, new Promise<Boolean>() {
            @Override
            public void then(Boolean aBoolean) {
                logRoomInfo("room writable: " + aBoolean);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void orientation(MenuItem item) {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            RoomActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            RoomActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    public void setBound(MenuItem item) {
        CameraBound bound = customBound(3);
        room.setCameraBound(bound);
    }

    public void nextScene(MenuItem item) {
        int nextIndex = room.getSceneState().getIndex() + 1;
        room.setSceneIndex(nextIndex, new Promise<Boolean>() {
            @Override
            public void then(Boolean result) {

            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    public void undoRedoOperation(MenuItem item) {
        // 需要开启本地序列化，才能操作 redo undo
        room.disableSerialization(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                room.undo();
            }
        }, 10000);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                room.redo();
            }
        }, 15000);
    }

    public void duplicate(MenuItem item) {
        room.duplicate();
    }

    public void copyPaste(MenuItem item) {
        room.copy();
        room.paste();
    }

    public void deleteOperation(MenuItem item) {
        room.deleteOperation();
    }

    public void getPreviewImage(MenuItem item) {
        room.getScenePreviewImage("/init", new Promise<Bitmap>() {
            @Override
            public void then(Bitmap bitmap) {
                logAction("get bitmap");
            }

            @Override
            public void catchEx(SDKError t) {
                logAction("get bitmap error");
            }
        });
    }

    public void getSceneImage(MenuItem item) {
        room.getSceneSnapshotImage("/init", new Promise<Bitmap>() {
            @Override
            public void then(Bitmap bitmap) {
                logAction("get bitmap");
            }

            @Override
            public void catchEx(SDKError t) {
                logAction("get bitmap error");
            }
        });
    }

    public void staticConvert(MenuItem item) {
        Converter c = new Converter(this.roomToken);
        c.startConvertTask("https://white-cn-edge-doc-convert.oss-cn-hangzhou.aliyuncs.com/LightWaves.pdf", Converter.ConvertType.Static, new ConverterCallbacks(){
            @Override
            public void onFailure(ConvertException e) {
                logAction(e.getMessage());
            }

            @Override
            public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {
                room.putScenes("/static", ppt.getScenes(), 0);
                room.setScenePath("/static/1");
                logAction(convertInfo.toString());
            }

            @Override
            public void onProgress(Double progress, ConversionInfo convertInfo) {
                logAction(String.valueOf(progress));
            }
        });
    }

    public void dynamicConvert(MenuItem item) {
        Converter c = new Converter(this.roomToken);
        c.startConvertTask("https://white-cn-edge-doc-convert.oss-cn-hangzhou.aliyuncs.com/-1/1.pptx", Converter.ConvertType.Dynamic, new ConverterCallbacks(){
            @Override
            public void onFailure(ConvertException e) {
                logAction(e.getMessage());
            }

            @Override
            public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {
                room.putScenes("/dynamic", ppt.getScenes(), 0);
                room.setScenePath("/dynamic/1");
                logAction(convertInfo.toString());
            }

            @Override
            public void onProgress(Double progress, ConversionInfo convertInfo) {
                logAction(String.valueOf(progress));
            }
        });
    }

    public void broadcast(MenuItem item) {
        logAction();
        room.setViewMode(ViewMode.Broadcaster);
    }

    public void getBroadcastState(MenuItem item) {
        logAction();
        BroadcastState broadcastState = room.getBroadcastState();
        showToast(broadcastState.getMode());
        logRoomInfo(gson.toJson(broadcastState));
    }

    public void moveCamera(MenuItem item) {
        logAction();
        CameraConfig config = new CameraConfig();
        config.setCenterX(100d);
        room.moveCamera(config);
    }

    public void moveRectangle(MenuItem item) {
        logAction();
        RectangleConfig config = new RectangleConfig(200d, 400d);
        room.moveCameraToContainer(config);
    }

    public void dispatchCustomEvent(MenuItem item) {
        logAction();
        HashMap<String, String> payload = new HashMap<>();
        payload.put("device", "android");

        room.dispatchMagixEvent(new AkkoEvent(EVENT_NAME, payload));
    }

    public void cleanScene(MenuItem item) {
        logAction();
        room.cleanScene(true);
    }

    public void insertNewScene(MenuItem item) {
        logAction();
        room.putScenes(SCENE_DIR, new Scene[]{
                new Scene("page1")}, 0);
        room.setScenePath(SCENE_DIR + "/page1");
    }

    public void insertPPT(MenuItem item) {
        logAction();
        room.putScenes(SCENE_DIR, new Scene[]{
            new Scene("page2", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg", 600d, 600d))
        }, 0);
        room.setScenePath(SCENE_DIR + "/page2");
    }

    public void insertImage(MenuItem item) {
        room.insertImage(new ImageInformationWithUrl(0d, 0d, 100d, 200d, "https://white-pan.oss-cn-shanghai.aliyuncs.com/40/image/mask.jpg"));
    }

    public void getScene(MenuItem item) {
        logAction();
        logAction(gson.toJson(room.getScenes()));
    }

    public void getRoomPhase(MenuItem item) {
        logAction();
        logRoomInfo("RoomPhase: " + gson.toJson(room.getRoomPhase()));
    }

    public void getRoomState(MenuItem item) {
        logAction();
        //获取房间状态，包含很多信息
        logRoomInfo("roomState: " + gson.toJson(room.getRoomState()));
    }

    public void disconnect(MenuItem item) {

        //如果需要房间断开连接后回调
        room.disconnect(new Promise<Object>() {
            @Override
            public void then(Object o) {
                logAction("disconnect success");
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });

        //如果不需要回调，则直接断开连接即可
        //room.disconnect();
    }

    public void disableOperation(MenuItem item) {
        logAction();
        room.disableOperations(true);
    }

    public void cancelDisableOperation(MenuItem item) {
        logAction();
        room.disableOperations(false);
    }

    public void textArea(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[]{99, 99, 99});
        memberState.setCurrentApplianceName(Appliance.TEXT);
        memberState.setStrokeWidth(10);
        memberState.setTextSize(10);
        room.setMemberState(memberState);
    }

    public void selector(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setCurrentApplianceName(Appliance.SELECTOR);
        room.setMemberState(memberState);
    }

    public void pencil(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[]{99, 99, 99});
        memberState.setCurrentApplianceName(Appliance.PENCIL);
        memberState.setStrokeWidth(10);
        memberState.setTextSize(10);
        room.setMemberState(memberState);
    }

    public void rectangle(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[]{99, 99, 99});
        memberState.setCurrentApplianceName(Appliance.RECTANGLE);
        memberState.setStrokeWidth(10);
        memberState.setTextSize(10);
        room.setMemberState(memberState);
    }

    public void color(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[]{200, 200, 200});
        memberState.setCurrentApplianceName(Appliance.PENCIL);
        memberState.setStrokeWidth(4);
        memberState.setTextSize(10);
        room.setMemberState(memberState);
    }

    public void convertPoint(MenuItem item) {
        //获取特定点，在白板内部的坐标点
        room.convertToPointInWorld(0, 0, new Promise<Point>() {
            @Override
            public void then(Point point) {
                logRoomInfo(gson.toJson(point));
            }

            @Override
            public void catchEx(SDKError t) {
//                Logger.error("convertToPointInWorld  error", t);
            }
        });
    }

    public void externalEvent(MenuItem item) {
        logAction();
    }

    public void zoomChange(MenuItem item) {
        CameraConfig cameraConfig = new CameraConfig();
        if (room.getZoomScale() != 1) {
            cameraConfig.setScale(1d);
        } else {
            cameraConfig.setScale(5d);
        }
        room.moveCamera(cameraConfig);
    }

    //endregion

    //region log
    void logRoomInfo(String str) {
        Log.i(ROOM_INFO, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction(String str) {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction() {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName());
    }

    void showToast(Object o) {
        Log.i("showToast", o.toString());
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }

    //endregion
}
