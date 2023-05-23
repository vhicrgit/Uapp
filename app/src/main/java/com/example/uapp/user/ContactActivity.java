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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.uapp.R;
import com.example.uapp.config.Config;
import com.example.uapp.thr.SetUserInfo;
import com.example.uapp.thr.UappService;
import com.example.uapp.utils.String2HashMap;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.HashMap;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ContactActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Button btn_save;
    private EditText et_qq;
    private EditText et_phone;
    private CheckBox cb_qq;
    private CheckBox cb_phone;

    private String origin_qq;
    private String origin_phone;
    private String new_qq;
    private String new_phone;
    private Boolean qq_enable = false;
    private Boolean phone_enable = false;
    private String origin_contact;
    private String new_contact;
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
        setContentView(R.layout.activity_contact);
        //登录信息
        pref = getSharedPreferences("login_info",MODE_PRIVATE);
        editor = getSharedPreferences("login_info",MODE_PRIVATE).edit();
        //导航栏及菜单
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("修改联系方式");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));
        //控件初始化
        btn_save = findViewById(R.id.btn_save);
        et_qq = findViewById(R.id.et_qq);
        et_phone = findViewById(R.id.et_phone);
        cb_qq = findViewById(R.id.cb_qq);
        cb_phone = findViewById(R.id.cb_phone);
        btn_save.setBackground(getResources().getDrawable(Config.themeColor_Button));
        //解析contact数据
        origin_contact = pref.getString("contact","");
        if(!origin_contact.equals("")){
            HashMap<String, String> parsedInfoMap = String2HashMap.stringToHashMap(origin_contact);
            origin_qq = parsedInfoMap.get("QQ");
            origin_phone = parsedInfoMap.get("Phone");
        }else{
            origin_qq = "";
            origin_phone = "";
        }
        //设置EditText
        et_qq.setText(origin_qq);
        et_phone.setText(origin_phone);
        //设置复选框
        qq_enable = !Objects.equals(origin_qq, "");
        phone_enable = !Objects.equals(origin_phone, "");
        cb_qq.setChecked(qq_enable);
        cb_phone.setChecked(true);


        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //检测是否选中联系渠道
                qq_enable = cb_qq.isChecked();
                phone_enable = cb_phone.isChecked();
                //根据enable修正结果
                new_qq = qq_enable ? et_qq.getText().toString() : "";
                new_phone = phone_enable ? et_phone.getText().toString() : "";
                if(Objects.equals(new_qq, origin_qq) && Objects.equals(new_phone, origin_phone)){
                    //检测到没修改
                    finish();
                }
                showConfirmationDialog();
            }
        });
    }

    private void showConfirmationDialog() {
        String message = "你确定要修改联系方式吗";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(message);
        // 设置"修改用户名"的样式
        int startIndex = message.indexOf("修改联系方式");
        int endIndex = startIndex + "修改联系方式".length();
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

    private void performAction() {
        new ChangeContactTask().execute();
    }

    private class ChangeContactTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 创建Item对象
            try {
                //将用户填写的信息转化为new_contact
                HashMap<String, String> infoMap = new HashMap<>();
                infoMap.put("QQ", new_qq);
                infoMap.put("Phone", new_phone);
                new_contact = String2HashMap.hashMapToString(infoMap);

                Boolean res;
                initializeUappServiceClient();
                SetUserInfo userInfo = new SetUserInfo();
                userInfo.setEmail("");
                userInfo.setContact(new_contact);
                userInfo.setPassword("");
                SharedPreferences sno = getSharedPreferences("login_info", Context.MODE_PRIVATE);
                userInfo.setStudent_id(sno.getString("sno","null"));
                userInfo.setUsername("");

                userInfo.setWhich(5);
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
                editor.putString("contact",new_contact);
                editor.apply();
                showMsg("新联系方式设置成功");
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {finish();}
        return super.onOptionsItemSelected(item);
    }
}