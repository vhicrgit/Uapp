package com.example.uapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uapp.config.Config;
import com.example.uapp.thr.DetailInfo;
import com.example.uapp.thr.ReqInfo;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class FoundItemDetailActivity extends AppCompatActivity {
    private SharedPreferences pref;
    TextView itemName;
    TextView lostTime;
    TextView lostPos;
    TextView contact;
    TextView description;
    ImageView imageView;
    Toolbar toolbar;

    private String item_name;
    private String post_id;
    private long lost_time;
    private String lost_pos;
    private String contact_;
    private String desc;
    private String image_path;

    private UappService.Client UappServiceClient;
    private void initializeUappServiceClient() throws TException {
        TTransport transport = new TSocket(getString(R.string.ip), getResources().getInteger(R.integer.port));
        TProtocol protocol = new TBinaryProtocol(transport);
        UappServiceClient = new UappService.Client(protocol);
        transport.open();
    }

    private void closeItemServiceClient() {
        if (UappServiceClient != null) {
            UappServiceClient.getInputProtocol().getTransport().close();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_item_detail);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        itemName = findViewById(R.id.item_name);
        lostTime = findViewById(R.id.lost_time);
        lostPos = findViewById(R.id.lost_pos);
        contact = findViewById(R.id.contact);
        description = findViewById(R.id.description);
        imageView = findViewById(R.id.image);
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));

        Intent intent = getIntent();
        item_name = intent.getStringExtra("itemName");
        post_id = intent.getStringExtra("postId");
        lost_time = intent.getLongExtra("lostTime",0);
        lost_pos = intent.getStringExtra("lostPos");
        contact_ = intent.getStringExtra("contact");
        desc = intent.getStringExtra("description");
        image_path = intent.getStringExtra("imagePath");

        itemName.setText(item_name);
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy年MM月dd日");
        String formattedDate1 = dateFormat1.format(new Date(lost_time));
        lostTime.setText(formattedDate1);
        lostPos.setText(lost_pos);
        contact.setText(contact_);
        description.setText(desc);

        new ReqImgTask().execute();



    }

    private class ReqImgTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // 创建Item对象
            try {
                DetailInfo detailInfo;
                ReqInfo reqInfo = new ReqInfo();
                initializeUappServiceClient();
                reqInfo.setFor_lost_item(false);
                reqInfo.setPost_id(post_id);
                detailInfo = UappServiceClient.reqDetail(reqInfo);

                //判断图片是否存在,若不存在,则创建图片
                File file = new File(image_path);
                Log.d("=======item_name======", ": "+ item_name);
                Log.d("=======image_path======", ": "+ image_path);
                if (!file.exists()) {
                    try {
                        byte [] image = detailInfo.getItem_image();
                        Log.d("=======debug_image======", ": "+ Arrays.toString(image));
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

                return true;
            } catch (TException e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean result){
            if(result){
                Log.d("=======debug_image_path=======", ": "+image_path);
                imageView.setImageBitmap(BitmapFactory.decodeFile(image_path));
            }
            else{
                Toast.makeText(FoundItemDetailActivity.this,"获取图片失败",
                        Toast.LENGTH_SHORT).show();
            }
        }
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
}
