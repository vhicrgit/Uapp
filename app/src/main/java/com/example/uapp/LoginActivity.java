package com.example.uapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import com.example.uapp.thr.LoginInfo;
import com.example.uapp.thr.RegisterInfo;
import com.example.uapp.thr.UappService;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private String sno;
    private String username = "";
    private String passward;
    private String email = "";
    private boolean loggedIn;
    private UappService.Client UappServiceClient;
    //控件
    private EditText et_passward;
    private EditText et_sno;



    private final ActivityResultLauncher<Intent> launcherActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                EditText et_sno = findViewById(R.id.et_sno);
                EditText et_passward = findViewById(R.id.et_passward);
                et_sno.setSaveEnabled(false);
                et_passward.setSaveEnabled(false);
                sno = result.getData().getStringExtra("data1");
                passward = result.getData().getStringExtra("data2");
                et_sno.setText(sno);
                et_passward.setText(passward);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView tv_register = findViewById(R.id.tv_register);
        et_sno = findViewById(R.id.et_sno);
        et_passward = findViewById(R.id.et_passward);
        Button btn_login = findViewById(R.id.btn_login);
        et_sno.setSaveEnabled(false);
        et_passward.setSaveEnabled(false);
        tv_register.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
               launcherActivity.launch(intent);
           }
       });
        //点击登录按钮，将用户名和密码保存至“login_info.xml”文件中
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoginTask().execute();
            }
        });
    }

    private void initializeUappServiceClient() throws TException {
        // 创建TTransport对象
        TTransport transport = new TSocket("202.38.72.73", 7860);
        // 创建TProtocol对象
        TProtocol protocol = new TBinaryProtocol(transport);
        // 创建ItemService.Client对象
        UappServiceClient = new UappService.Client(protocol);
        // 打开transport
        transport.open();
    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Boolean logInSuccess;
                initializeUappServiceClient();
                //登录
                LoginInfo loginInfo = new LoginInfo();
                loginInfo.setStudent_id(sno);
                loginInfo.setPassword(passward);
                logInSuccess = UappServiceClient.login(loginInfo);
                if(logInSuccess) {
                    //获取用户信息
                    RegisterInfo userInfo;
                    userInfo = UappServiceClient.getUserInfo(sno);
                    email = userInfo.getEmail();
                    username = userInfo.getUsername();
                }
                return logInSuccess;
            } catch (TException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(result){
                Toast.makeText(LoginActivity.this,"登陆成功！",
                        Toast.LENGTH_SHORT).show();
                passward = et_passward.getText().toString();
                loggedIn = true;
                SharedPreferences.Editor editor = getSharedPreferences("login_info",MODE_PRIVATE).edit();
                editor.putString("sno",sno);
                editor.putString("username",username);
                editor.putString("passward",passward);
                editor.putString("email",email);
                editor.putBoolean("loggedIn",loggedIn);
                editor.apply();
                finish();
            }
            else{
                Toast.makeText(LoginActivity.this,"密码错误或学号不存在",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
