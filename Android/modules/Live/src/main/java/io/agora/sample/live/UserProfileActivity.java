package io.agora.sample.live;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.agora.uiwidget.basic.TitleBar;

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        TitleBar titleBar = findViewById(R.id.title_bar);
        titleBar.setTitleName("UserProfile", Color.BLACK);
        titleBar.setBackIcon(true, io.agora.uiwidget.R.drawable.title_bar_back_black, v -> {
            finish();
        });
    }

}
