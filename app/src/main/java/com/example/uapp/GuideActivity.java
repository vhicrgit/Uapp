package com.example.uapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private List<View> viewsList = new ArrayList<>();
    private Button toMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        LayoutInflater inflater = LayoutInflater.from(this);
        View guide_one = inflater.inflate(R.layout.guide_one, null);
        View guide_two = inflater.inflate(R.layout.guide_two, null);
        View guide_three = inflater.inflate(R.layout.guide_three, null);
        viewsList.add(guide_one);
        viewsList.add(guide_two);
        viewsList.add(guide_three);
        pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return viewsList.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object; // 判断当前的view是我们所需的对象
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                View view = viewsList.get(position);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                View view = viewsList.get(position);
                container.removeView(view);
            }
        };
        viewPager.setAdapter(pagerAdapter);
        toMain = (Button) guide_three.findViewById(R.id.to_main);
        toMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GuideActivity.this, MainActivity.class));
                finish();
            }
        });

    }
}