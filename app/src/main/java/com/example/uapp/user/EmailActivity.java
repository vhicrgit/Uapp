package com.example.uapp.user;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.uapp.R;
import com.example.uapp.config.Config;
import com.example.uapp.thr.SetUserInfo;
import com.example.uapp.thr.UappService;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class EmailActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Button btn_save;
    private EditText et_email;
    private String origin_email;
    private String new_email;
    private UappService.Client UappServiceClient;

    private void initializeUappServiceClient() throws TException {
        TTransport transport = new TSocket(getString(R.string.ip), getResources().getInteger(R.integer.port));
        TProtocol protocol = new TBinaryProtocol(transport);
        UappServiceClient = new UappService.Client(protocol);
        transport.open();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        //登录信息
        pref = getSharedPreferences("login_info",MODE_PRIVATE);
        editor = getSharedPreferences("login_info",MODE_PRIVATE).edit();
        //导航栏及菜单
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("修改邮箱");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));
        //控件初始化
        btn_save = findViewById(R.id.btn_save);
        et_email = findViewById(R.id.et_email);
        origin_email = pref.getString("email","");
        et_email.setText(origin_email);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new_email = et_email.getText().toString();
                if(!new_email.equals(origin_email)){
                    //检测到用户修改了用户名
                    showConfirmationDialog();
                }else {
                    finish();
                }
            }
        });
    }

    private void showConfirmationDialog() {
        String message = "你确定要修改邮箱吗";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(message);
        // 设置"修改用户名"的样式
        int startIndex = message.indexOf("修改邮箱");
        int endIndex = startIndex + "修改邮箱".length();
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
        spannableStringBuilder.setSpan(boldSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认");
        builder.setMessage(spannableStringBuilder);
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

    private void performAction() {new ChangeEmailTask().execute();}

    private class ChangeEmailTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 创建Item对象
            try {
                Boolean res;
                initializeUappServiceClient();
                SetUserInfo userInfo = new SetUserInfo();
                userInfo.setEmail(new_email);
                userInfo.setContact("");
                userInfo.setPassword("");
                SharedPreferences sno = getSharedPreferences("login_info", Context.MODE_PRIVATE);
                userInfo.setStudent_id(sno.getString("sno","null"));
                userInfo.setUsername("");
                userInfo.setWhich(3);
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
                editor.putString("email",new_email);
                editor.apply();
                showMsg("新邮箱设置成功");
                finish();
            }
            else{
                showMsg("设置失败");
            }
        }
    }
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {finish();}
        return super.onOptionsItemSelected(item);
    }
}