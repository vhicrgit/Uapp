package com.example.uapp.user;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.uapp.R;
import com.example.uapp.config.Config;
import com.example.uapp.thr.UappService;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ThemeActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Button btn_white;
    private Button btn_black;
    private Button btn_pink;
    private Button btn_red;
    private Button btn_yellow;
    private Button btn_green;
    private Button btn_blue;
    private Button btn_purple;
    private LinearLayout ll_pink;
    private LinearLayout ll_white;
    private LinearLayout ll_black;
    private LinearLayout ll_red;
    private LinearLayout ll_yellow;
    private LinearLayout ll_green;
    private LinearLayout ll_blue;
    private LinearLayout ll_purple;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        //登录信息
        pref = getSharedPreferences("login_info",MODE_PRIVATE);
        editor = getSharedPreferences("login_info",MODE_PRIVATE).edit();
        //导航栏及菜单
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("修改主题颜色");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));

        //控件初始化
        ll_pink = findViewById(R.id.ll_pink);
        ll_white = findViewById(R.id.ll_white);
        ll_black = findViewById(R.id.ll_black);
        ll_red = findViewById(R.id.ll_red);
        ll_yellow = findViewById(R.id.ll_yellow);
        ll_green = findViewById(R.id.ll_green);
        ll_blue = findViewById(R.id.ll_blue);
        ll_purple = findViewById(R.id.ll_purple);
        btn_pink = findViewById(R.id.btn_pink);
        btn_red = findViewById(R.id.btn_red);
        btn_white = findViewById(R.id.btn_white);
        btn_black = findViewById(R.id.btn_black);
        btn_yellow = findViewById(R.id.btn_yellow);
        btn_green = findViewById(R.id.btn_green);
        btn_blue = findViewById(R.id.btn_blue);
        btn_purple = findViewById(R.id.btn_purple);
        //选择颜色
        btn_pink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_pink.setBackgroundResource(R.drawable.rounded_background_white);
                ll_white.setBackground(null);
                ll_black.setBackground(null);
                ll_red.setBackground(null);
                downSetNull();
                Config.themeColor = Config.themeColor_Pink;
                Config.themeColor_Button = Config.themeColor_Button_Pink;
                Config.themeColor_Text = Config.themeColor_Text_Pink;
                saveAndSet();
            }
        });
       btn_white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_white.setBackgroundResource(R.drawable.rounded_background_white);
                ll_pink.setBackground(null);
                ll_black.setBackground(null);
                ll_red.setBackground(null);
                downSetNull();
                Config.themeColor = Config.themeColor_White;
                Config.themeColor_Button = Config.themeColor_Button_White;
                Config.themeColor_Text = Config.themeColor_Text_White;
                saveAndSet();
            }
        });
        btn_black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_black.setBackgroundResource(R.drawable.rounded_background_white);
                ll_white.setBackground(null);
                ll_pink.setBackground(null);
                ll_red.setBackground(null);
                downSetNull();
                Config.themeColor = Config.themeColor_Black;
                Config.themeColor_Button = Config.themeColor_Button_Black;
                Config.themeColor_Text = Config.themeColor_Text_Black;
                saveAndSet();
            }
        });
        btn_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_red.setBackgroundResource(R.drawable.rounded_background_white);
                ll_white.setBackground(null);
                ll_black.setBackground(null);
                ll_pink.setBackground(null);
                downSetNull();
                Config.themeColor = Config.themeColor_Red;
                Config.themeColor_Button = Config.themeColor_Button_Red;
                Config.themeColor_Text = Config.themeColor_Text_Red;
                saveAndSet();
            }
        });
        btn_yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_yellow.setBackgroundResource(R.drawable.rounded_background_white);
                ll_purple.setBackground(null);
                ll_green.setBackground(null);
                ll_blue.setBackground(null);
                upSetNull();
                Config.themeColor = Config.themeColor_Yellow;
                Config.themeColor_Button = Config.themeColor_Button_Yellow;
                Config.themeColor_Text = Config.themeColor_Text_Yellow;
                saveAndSet();
            }
        });
        btn_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_green.setBackgroundResource(R.drawable.rounded_background_white);
                ll_yellow.setBackground(null);
                ll_purple.setBackground(null);
                ll_blue.setBackground(null);
                upSetNull();
                Config.themeColor = Config.themeColor_Green;
                Config.themeColor_Button = Config.themeColor_Button_Green;
                Config.themeColor_Text = Config.themeColor_Text_Green;
                saveAndSet();
            }
        });
        btn_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_blue.setBackgroundResource(R.drawable.rounded_background_white);
                ll_yellow.setBackground(null);
                ll_green.setBackground(null);
                ll_purple.setBackground(null);
                upSetNull();
                Config.themeColor = Config.themeColor_Blue;
                Config.themeColor_Button = Config.themeColor_Button_Blue;
                Config.themeColor_Text = Config.themeColor_Text_Blue;
                saveAndSet();
            }
        });
        btn_purple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_purple.setBackgroundResource(R.drawable.rounded_background_white);
                ll_yellow.setBackground(null);
                ll_green.setBackground(null);
                ll_blue.setBackground(null);
                upSetNull();
                Config.themeColor = Config.themeColor_Purple;
                Config.themeColor_Button = Config.themeColor_Button_Purple;
                Config.themeColor_Text = Config.themeColor_Text_Purple;
                saveAndSet();
            }
        });
    }

    private void showConfirmationDialog() {
        String message = "你确定要修改常用地址吗";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(message);
        // 设置"修改用户名"的样式
        int startIndex = message.indexOf("修改常用地址");
        int endIndex = startIndex + "修改常用地址".length();
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

    }
    private void saveAndSet(){
        editor.putInt("themeColor",Config.themeColor);
        editor.putInt("themeColor_Text",Config.themeColor_Text);
        editor.putInt("themeColor_Button",Config.themeColor_Button);
        editor.apply();
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {finish();}
        return super.onOptionsItemSelected(item);
    }

    private void upSetNull(){
        ll_black.setBackground(null);
        ll_white.setBackground(null);
        ll_pink.setBackground(null);
        ll_red.setBackground(null);
    }

    private void downSetNull(){
        ll_yellow.setBackground(null);
        ll_green.setBackground(null);
        ll_blue.setBackground(null);
        ll_purple.setBackground(null);
    }
}