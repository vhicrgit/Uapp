package com.example.uapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uapp.config.Config;
import com.example.uapp.thr.RegisterInfo;
import com.example.uapp.thr.UappService;
import com.example.uapp.utils.AppearanceUtils;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RegisterActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private String sno;
    private String username;
    private String passward;
    private String confirm;
    private String email;

    private UappService.Client UappServiceClient;
    private void initializeUappServiceClient() throws TException {
        // 创建TTransport对象
        Log.d("MyApp", "In initializeItemServiceClient!");
        TTransport transport = new TSocket(getString(R.string.ip), getResources().getInteger(R.integer.port));
        Log.d("MyApp", "TSocket!");
        // 创建TProtocol对象
        TProtocol protocol = new TBinaryProtocol(transport);
        Log.d("MyApp", "protocol!");
        // 创建ItemService.Client对象
        UappServiceClient = new UappService.Client(protocol);
        Log.d("MyApp", "itemServiceClient!");
        // 打开transport
        transport.open();
        Log.d("MyApp", "transport.open()!");
    }

    private void closeItemServiceClient() {
        if (UappServiceClient != null) {
            UappServiceClient.getInputProtocol().getTransport().close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //导航栏及菜单
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("注册");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));

        EditText et_sno = findViewById(R.id.et_sno);
        EditText et_username = findViewById(R.id.et_username);
        EditText et_passward = findViewById(R.id.et_passward);
        EditText et_confirm = findViewById(R.id.et_confirm);
        EditText et_email = findViewById(R.id.et_email);
        Button btn_register = findViewById(R.id.btn_register);
        btn_register.setBackground(getResources().getDrawable(Config.themeColor_Button));
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passward = et_passward.getText().toString();
                confirm = et_confirm.getText().toString();
                sno = et_sno.getText().toString();
                username = et_username.getText().toString();
                email = et_email.getText().toString();
                //检验两次输入密码是否无误
                if(!Objects.equals(passward, confirm)){
                    showMsg("两次输入密码不相同，请重新输入");
                    return;
                }
                //检测邮箱是否是科大邮箱
                //TODO
                //检测密码是否过短
                if(passward.length() < 6){
                    showMsg("密码长度应不少于6位");
                }
                //检测用户名是否过短
                if(username.length() < 4){
                    showMsg("用户名过短");
                }
                //检测学号是否过短
                if(sno.length() < 4){
                    showMsg("学号错误");
                }
                //......
                new RegisterTask().execute();
            }
        });
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

    private class RegisterTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // 创建Item对象
            try {
                Boolean reactFromServer;
                initializeUappServiceClient();
                RegisterInfo regist = new RegisterInfo();
                regist.setEmail(email);
                regist.setStudent_id(sno);
                regist.setPassword(passward);
                regist.setUsername(username);
                regist.setContact("");
//                regist.setHeadshot(null);
                reactFromServer = UappServiceClient.register(regist);
                return reactFromServer;
            } catch (TException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(result){
                Toast.makeText(RegisterActivity.this,"新用户注册成功！",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("data1",sno);
                intent.putExtra("data2",passward);
                setResult(RESULT_OK,intent);
                finish();
            }
            else{
                Toast.makeText(RegisterActivity.this,"该学号已被注册",
                        Toast.LENGTH_SHORT).show();
            }
            closeItemServiceClient();
        }
    }
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
