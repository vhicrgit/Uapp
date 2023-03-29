package com.example.uapp;

import android.app.Activity;
import android.app.Application;

import java.util.List;

public class MainApplication extends Application {
    private static MainApplication mApp; // 声明一个当前应用的静态实例

    // 利用单例模式获取当前应用的唯一实例
    public static MainApplication getInstance() {
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this; // 在打开应用时对静态的应用实例赋值
    }
}
