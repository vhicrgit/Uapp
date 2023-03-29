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
    private Button button;
    private UappTimer timer;
    private boolean isFirstIn;

    private Handler handler = new Handler();

    private Runnable toNextActivity = new Runnable() {
        @Override
        public void run() {
            if(isFirstIn) {
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
        isFirstIn = sfile.getBoolean("isFirst", true);
        SharedPreferences.Editor editor = sfile.edit();

        if(isFirstIn) {
            editor.putBoolean("isFirst", false);
        }
        editor.commit();

        timer = new UappTimer(4000, 1000);
        timer.start();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toNextActivity.run();
                timer.onFinish();
            }
        });

        handler.postDelayed(toNextActivity, 3000);
    }

    class UappTimer extends CountDownTimer {

        public UappTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long l) {
            button.setText(l/1000 + " s");
        }

        @Override
        public void onFinish() {
            handler.removeCallbacks(toNextActivity);
        }
    }
}