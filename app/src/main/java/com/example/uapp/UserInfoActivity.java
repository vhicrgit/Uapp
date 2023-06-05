package com.example.uapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.allen.library.SuperTextView;
import com.example.uapp.config.Config;
import com.example.uapp.user.ContactActivity;
import com.example.uapp.user.EmailActivity;
import com.example.uapp.user.UsernameActivity;
import com.example.uapp.utils.AppearanceUtils;

import java.util.Objects;

public class UserInfoActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SuperTextView tv_sno;
    private SuperTextView tv_username;
    private SuperTextView tv_email;
    private SuperTextView tv_contact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        //导航栏及菜单
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("用户信息");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//添加默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));
        //控件初始化
        tv_sno = findViewById(R.id.tv_sno);
        tv_username = findViewById(R.id.tv_username);
        tv_email = findViewById(R.id.tv_email);
        tv_contact = findViewById(R.id.tv_contact);
        //设置右部Text
        pref = getSharedPreferences("login_info", MODE_PRIVATE);
        tv_sno.setRightString(pref.getString("sno",""));
        tv_username.setRightString(pref.getString("username",""));
        tv_email.setRightString(pref.getString("email",""));
        //点击事件
        tv_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserInfoActivity.this,UsernameActivity.class);
                startActivity(intent);
            }
        });
        tv_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserInfoActivity.this, EmailActivity.class);
                startActivity(intent);
            }
        });
        tv_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserInfoActivity.this, ContactActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        tv_sno.setRightString(pref.getString("sno",""));
        tv_username.setRightString(pref.getString("username",""));
        tv_email.setRightString(pref.getString("email",""));
    }

    protected void onStart() {
        super.onStart();
        ViewGroup rootView = findViewById(android.R.id.content);
        ViewTreeObserver observer = rootView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                pref = getSharedPreferences("login_info", Context.MODE_PRIVATE);
                if(pref.getBoolean("careMode",false)){
                    //关怀模式
                    AppearanceUtils.increaseFontSize(rootView,1.25f);
                }
                // 移除监听器，避免重复回调
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}