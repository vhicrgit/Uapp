package com.example.uapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.uapp.thr.LoginInfo;
import com.example.uapp.thr.RegisterInfo;
import com.example.uapp.thr.SetUserInfo;
import com.example.uapp.thr.UappService;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class ChangePasswardActivity extends AppCompatActivity {
    //
    private String old_pwd;
    private String new_pwd;
    private String new_pwd_confirm;
    //
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    //错误计数
    private int error_cnt = 0;
    //控件
    private Toolbar toolbar;
    private EditText et_old_pwd;
    private EditText et_new_pwd;
    private EditText et_new_pwd_confirm;


    private Button btn_submit;
    //与服务器通信
    private UappService.Client UappServiceClient;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_passward);
        //toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("修改密码");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //控件
        et_old_pwd = findViewById(R.id.et_old_password);
        et_new_pwd = findViewById(R.id.et_new_password);
        et_new_pwd_confirm = findViewById(R.id.et_again_password);
        //登录信息
        pref = getSharedPreferences("login_info",MODE_PRIVATE);
        editor = getSharedPreferences("login_info",MODE_PRIVATE).edit();
        //确认按钮
        btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String saved_passward;
                saved_passward = pref.getString("passward","");
                old_pwd = et_old_pwd.getText().toString();
                new_pwd = et_new_pwd.getText().toString();
                new_pwd_confirm = et_new_pwd_confirm.getText().toString();
                if(!Objects.equals(old_pwd, saved_passward)){
                    if(error_cnt == 2){
                        showMsg("密码输入错误次数过多");
                        finish();
                    }
                    error_cnt++;
                    showMsg("密码错误，还有"+(3-error_cnt)+"次机会");
                    return;
                }
                if(new_pwd.isEmpty()){
                    showMsg("新密码不能为空");
                    return;
                }
                if(!Objects.equals(new_pwd, new_pwd_confirm)){
                    showMsg("两次输入新密码不一致");
                    return;
                }
                showConfirmationDialog();
            }
        });
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认");
        builder.setMessage("你确定要执行此操作吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击了确认按钮
                performAction();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击了取消按钮
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void performAction() {
        new ChangePasswardTask().execute();
    }

    private class ChangePasswardTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 创建Item对象
            try {
                Boolean res;
                initializeUappServiceClient();
                SetUserInfo userInfo = new SetUserInfo();
                userInfo.setEmail("");
                userInfo.setContact("");
                userInfo.setPassword(new_pwd);
                SharedPreferences sno = getSharedPreferences("login_info", Context.MODE_PRIVATE);
                userInfo.setStudent_id(sno.getString("sno","null"));
                userInfo.setUsername("");
                userInfo.setWhich(4);

                res = UappServiceClient.setUserInfo(userInfo);
                return res;
            } catch (TException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(result){
                editor.putString("passward",new_pwd);
                editor.apply();
                showMsg("新密码设置成功");
                finish();
            }
            else{
                showMsg("新密码设置失败");
            }
        }
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}