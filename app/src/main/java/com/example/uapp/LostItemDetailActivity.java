package com.example.uapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LostItemDetailActivity extends AppCompatActivity {
    TextView itemName;
    TextView lostTime;
    TextView lostPos;
    TextView contact;
    TextView description;
    ImageView imageView;
    Toolbar toolbar;

    private String item_name;
    private long lost_time;
    private String lost_pos;
    private String contact_;
    private String desc;
    private String image_path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_item_detail);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        itemName = findViewById(R.id.item_name);
        lostTime = findViewById(R.id.lost_time);
        lostPos = findViewById(R.id.lost_pos);
        contact = findViewById(R.id.contact);
        description = findViewById(R.id.description);
        imageView = findViewById(R.id.image);

        Intent intent = getIntent();
        item_name = intent.getStringExtra("data");
        lost_time = intent.getLongExtra("lostTime",0);
        lost_pos = intent.getStringExtra("lostPos");
        contact_ = intent.getStringExtra("contact");
        image_path = intent.getStringExtra("image");
        desc = intent.getStringExtra("description");

        itemName.setText(item_name);
        lostTime.setText(new Date(lost_time).toString());
        lostPos.setText(lost_pos);
        contact.setText(contact_);
        description.setText(image_path);
        imageView.setImageBitmap(BitmapFactory.decodeFile(image_path));





    }


}
