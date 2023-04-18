package com.example.uapp.task.util;

import android.app.Activity;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


public class SocketUtil {

    // 判断Socket能否连通
    public static void checkSocketAvailable(Activity act, String host, int port) {
        new Thread(() -> {
            try (java.net.Socket socket = new java.net.Socket()) {
                SocketAddress address = new InetSocketAddress(host, port);
                socket.connect(address, 1500);
            } catch (Exception e) {
                e.printStackTrace();
                act.runOnUiThread(() -> {
                    Toast.makeText(act, "无法连接Socket服务器", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

}