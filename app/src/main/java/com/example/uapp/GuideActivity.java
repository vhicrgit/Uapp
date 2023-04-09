package com.example.uapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends AppCompatActivity {

    private final List<View> viewsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        LayoutInflater inflater = LayoutInflater.from(this);

        @SuppressLint("InflateParams") View guide_lost_and_found = inflater.inflate(R.layout.guide_lost_and_found, null);
        @SuppressLint("InflateParams") View guide_learn_help = inflater.inflate(R.layout.guide_learn_help, null);
        @SuppressLint("InflateParams") View guide_information_integration = inflater.inflate(R.layout.guide_information_integration, null);

        viewsList.add(guide_lost_and_found);
        viewsList.add(guide_learn_help);
        viewsList.add(guide_information_integration);
        // 判断当前的view是我们所需的对象
        PagerAdapter pagerAdapter = new PagerAdapter() {
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
        Button toMain = (Button) guide_information_integration.findViewById(R.id.to_main);
        toMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GuideActivity.this, MainActivity.class));
                finish();
            }
        });

    }
}