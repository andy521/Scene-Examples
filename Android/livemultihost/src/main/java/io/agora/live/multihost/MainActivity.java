package io.agora.live.multihost;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        findViewById(R.id.btn_join).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomName = ((EditText)findViewById(R.id.et_room_name)).getText().toString();
                if(TextUtils.isEmpty(roomName)){
                    Toast.makeText(MainActivity.this, "The room name is empty", Toast.LENGTH_LONG).show();
                    return;
                }

                AndPermission.with(MainActivity.this)
                        .runtime()
                        .permission(Permission.Group.STORAGE, Permission.Group.MICROPHONE, Permission.Group.CAMERA)
                        .onGranted(data -> {
                            startActivity(MultiHostActivity.launch(MainActivity.this, roomName));
                        })
                        .start();


            }
        });
    }


}