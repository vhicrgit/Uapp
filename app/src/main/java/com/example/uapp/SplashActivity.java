package com.example.uapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class SplashActivity extends AppCompatActivity {

    private static final int DELAY_TIME = 3000;  // 单位ms
    private static final int GO_MAIN = 100;
    private static final int GO_GUIDE = 101;
    private Button button;
    private UappTimer timer;

    private boolean isFirst;

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(isFirst) {
                startActivity(new Intent(SplashActivity.this, GuideActivity.class));

            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        button = (Button) findViewById(R.id.button);

        SharedPreferences sfile = getSharedPreferences("data", MODE_PRIVATE);  // 判断用户是否第一次进入,如果是，进入引导页
        isFirst = sfile.getBoolean("isFirst", true);
        SharedPreferences.Editor editor = sfile.edit();
        if(isFirst) {
            editor.putBoolean("isFirst", false);
        }
        editor.commit();

        timer = new UappTimer(4000, 1000);
        timer.start();

        handler.postDelayed(runnable, DELAY_TIME);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFirst) {
                    startActivity(new Intent(SplashActivity.this, GuideActivity.class));

                } else {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                finish();
            }
        });
    }

    class UappTimer extends CountDownTimer {

        public UappTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long l) {
            button.setText(l/1000 + "秒");
        }

        @Override
        public void onFinish() {
            handler.removeCallbacks(runnable);
        }
    }
}