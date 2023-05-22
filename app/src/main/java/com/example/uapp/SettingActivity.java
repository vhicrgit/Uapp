package com.example.uapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.allen.library.SuperTextView;
import com.example.uapp.config.Config;
import com.example.uapp.user.ContactActivity;
import com.example.uapp.user.EmailActivity;
import com.example.uapp.user.ThemeActivity;
import com.example.uapp.user.UsernameActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingActivity extends AppCompatActivity {
    private SuperTextView tv_theme;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //导航栏及菜单
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("设置");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));
        //控件初始化
        tv_theme = findViewById(R.id.tv_theme);
        //点击事件
        tv_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, ThemeActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onResume() {
        super.onResume();
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));
    }

}