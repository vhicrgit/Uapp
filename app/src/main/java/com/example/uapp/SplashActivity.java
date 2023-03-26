package com.example.uapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private Handler handler = new Handler();
    UappTimer timer;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            toMainActivity();
        }
    };

    private void toMainActivity() {
        startActivity(new Intent(this, MainActivity_main.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        handler.postDelayed(runnable, 3000);
        timer = new UappTimer(4000, 1000);
        timer.start();
    }

    private void initViews() {
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toMainActivity();
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