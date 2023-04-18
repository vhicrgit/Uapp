package com.example.uapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private String username;
    private String passward;

    private final ActivityResultLauncher<Intent> launcherActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                EditText et_username = findViewById(R.id.et_username);
                EditText et_passward = findViewById(R.id.et_passward);
                et_username.setSaveEnabled(false);
                et_passward.setSaveEnabled(false);
                username = result.getData().getStringExtra("data1");
                passward = result.getData().getStringExtra("data2");
                et_username.setText(username);
                et_passward.setText(passward);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView tv_register = findViewById(R.id.tv_register);
        EditText et_username = findViewById(R.id.et_username);
        EditText et_passward = findViewById(R.id.et_passward);
        et_username.setSaveEnabled(false);
        et_passward.setSaveEnabled(false);
        tv_register.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
               launcherActivity.launch(intent);
           }
       });
    }
}
