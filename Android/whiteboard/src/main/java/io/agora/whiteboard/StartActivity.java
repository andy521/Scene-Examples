package io.agora.whiteboard;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.herewhite.whiteboard.R;

import pub.devrel.easypermissions.EasyPermissions;

public class StartActivity extends AppCompatActivity {

    public static final String ROLE = "role";
    public static final String CHANNEL_NAME = "channel";
    public static final String HOST = "host";
    public static final String AUDIENCE = "audience";
    DemoAPI demoAPI = new DemoAPI();

    private static final int TAG_PERMISSTION_REQUESTCODE = 1000;
    private static final String[] PERMISSTION = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        demoAPI.downloadZip("https://convertcdn.netless.link/dynamicConvert/e1ee27fdb0fc4b7c8f649291010c4882.zip", getCacheDir().getAbsolutePath());
    }

    String getChannel() {
        EditText text = findViewById(R.id.editText);
        return text.getText().toString();
    }

    void tokenAlert() {
        tokenAlert("token", "请在 https://console.herewhite.com 中注册，并获取 sdk token，再进行使用");
    }

    void tokenAlert(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(StartActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void grantPermissions(){
        if (!EasyPermissions.hasPermissions(this, PERMISSTION)) {
            EasyPermissions.requestPermissions(this, getString(R.string.error_permisstion),
                    TAG_PERMISSTION_REQUESTCODE, PERMISSTION);
        }
    }

    public void joinNewRoom(View view) {
        if (!demoAPI.validateToken()) {
            tokenAlert();
            return;
        }
        Intent intent = new Intent(this, RoomActivity.class);
        grantPermissions();
        String channel = getChannel();
        if (channel.length() > 0) {
            intent.putExtra(CHANNEL_NAME, channel);
        }
        intent.putExtra(ROLE, HOST);
        startActivity(intent);
    }

    public void joinRoom(View view) {
        if (!demoAPI.validateToken()) {
            tokenAlert();
            return;
        }
        Intent intent = new Intent(this, RoomActivity.class);
        grantPermissions();
        String channel = getChannel();
        if (channel.length() > 0) {
            intent.putExtra(CHANNEL_NAME, channel);
        }
        intent.putExtra(ROLE, AUDIENCE);
        startActivity(intent);
    }

}
