package com.example.uapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import com.example.uapp.config.Config;
import com.example.uapp.thr.LoginInfo;
import com.example.uapp.thr.RegisterInfo;
import com.example.uapp.thr.UappService;
import com.example.uapp.utils.AppearanceUtils;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private String sno;
    private String username = "";
    private String contact = "";
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
        //导航栏及菜单
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("登录");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));
        //控件初始化
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
        btn_login.setBackground(getResources().getDrawable(Config.themeColor_Button));
        btn_login.setBackground(getResources().getDrawable(Config.themeColor_Button));
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoginTask().execute();
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


    private void initializeUappServiceClient() throws TException {
        // 创建TTransport对象
        TTransport transport = new TSocket(getString(R.string.ip), getResources().getInteger(R.integer.port));
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
                    contact = userInfo.getContact();

                    if(userInfo.getHeadshot() != null && userInfo.getHeadshot().length != 0){
                        Log.d("=======debug_head_shot=======", " "+ Arrays.toString(userInfo.getHeadshot()));
                        Log.d("=======debug_head_shot=======", "len: "+ userInfo.getHeadshot().length);

                        File file = new File(getFilesDir(),"headshot.jpg");
                        if (!file.exists()) {
                            try {
                                byte [] image = userInfo.getHeadshot();
                                if(Arrays.toString(image).equals("null")){
                                    return false;
                                }
                                // 将 byte[] 数据转换为 Bitmap
                                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                                // 将 Bitmap 保存为文件
                                FileOutputStream outputStream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                outputStream.flush();
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
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
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
            else{
                Toast.makeText(LoginActivity.this,"密码错误或学号不存在",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


}
