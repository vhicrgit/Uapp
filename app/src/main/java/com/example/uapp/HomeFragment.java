package com.example.uapp;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.uapp.config.Config;
import com.example.uapp.task.GetAddressTask;
import com.example.uapp.task.util.DateUtil;
import com.example.uapp.task.util.SocketUtil;
import com.example.uapp.utils.AppearanceUtils;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;


public class HomeFragment extends Fragment {
    private ImageView imageShow;
    private TextView textLocation;


    private SharedPreferences pref;
    private SharedPreferences.Editor editor;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //获取主题
        pref = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
        Config.themeColor = pref.getInt("themeColor",Config.themeColor_White);
        Config.themeColor_Text = pref.getInt("themeColor_Text",Config.themeColor_Text_White);

        Button btn_post_lost = view.findViewById(R.id.btn_post_lost);
        Button btn_post_found = view.findViewById(R.id.btn_post_found);
//        imageShow = view.findViewById(R.id.show_photo);
        //导航栏及菜单
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("主页");
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));


        btn_post_lost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PostLostActivity.class);
                startActivity(intent);
            }
        });
        btn_post_found.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PostFoundActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewTreeObserver observer = getView().getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                pref = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
                if(pref.getBoolean("careMode",false)){
                    //关怀模式
                    AppearanceUtils.increaseFontSize(getView(),1.25f);
                }
                // 移除监听器，避免重复回调
                getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }
}

