package com.example.uapp;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uapp.config.Config;
import com.example.uapp.task.GetAddressTask;
import com.example.uapp.task.util.DateUtil;
import com.example.uapp.task.util.SocketUtil;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;


public class HomeFragment extends Fragment {
    private final Map<String,String> providerMap = new HashMap<>();
    private ImageView imageShow;
    private TextView textLocation;

    private String mLocationDesc = ""; // 定位说明
    private LocationManager mLocationMgr; // 声明一个定位管理器对象
    private final Criteria criteria = new Criteria(); // 创建一个定位准则对象
    private final Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象
    private boolean isLocationEnable = false; // 定位服务是否可用

    private static final int GET_THUMBNAIL_CODE = 1;
    private Bitmap mBitmap; // 位图对象

    private static final int BUFFER_SIZE = 1024 * 8;
    private Socket mSocket; // 声明一个套接字对象

    // 定义一个刷新任务，若无法定位则每隔一秒就尝试定位
    private final Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            if (!isLocationEnable) {
                initLocation(); // 初始化定位服务
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    // 定义一个位置变更监听器
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location); // 显示定位结果文本
        }

        @Override
        public void onProviderDisabled(String arg0) {}

        @Override
        public void onProviderEnabled(String arg0) {}

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
    };



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Button buttonTakePhoto = view.findViewById(R.id.take_photo);
        Button buttonUploadPhoto = view.findViewById(R.id.upload_photo);
        Button btn_post_lost = view.findViewById(R.id.btn_post_lost);
        imageShow = view.findViewById(R.id.show_photo);
        textLocation = view.findViewById(R.id.show_location);

        providerMap.put("gps", "卫星定位");
        providerMap.put("network", "网络定位");


        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(photoIntent, GET_THUMBNAIL_CODE);
            }
        });

        buttonUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBitmap =  ((BitmapDrawable)imageShow.getDrawable()).getBitmap();
                sendImage();
            }
        });

        btn_post_lost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PostLostActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.removeCallbacks(mRefresh); // 移除定位刷新任务
        initLocation(); // 初始化定位服务
        mHandler.postDelayed(mRefresh, 100); // 延迟100毫秒启动定位刷新任务
        // 初始化套接字
        initSocket();
    }

    // 初始化定位服务
    @SuppressLint("SetTextI18n")
    private void initLocation() {
        // 从系统服务中获取定位管理器
        mLocationMgr = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 设置定位精确度
        criteria.setAltitudeRequired(true); // 设置是否需要海拔信息
        criteria.setBearingRequired(true); // 设置是否需要方位信息
        criteria.setCostAllowed(true); // 设置是否允许运营商收费
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 设置对电源的需求
        // 获取定位管理器的最佳定位提供者
        String bestProvider = mLocationMgr.getBestProvider(criteria, true);

        if (bestProvider != null && mLocationMgr.isProviderEnabled(bestProvider)) { // 定位提供者当前可用
            textLocation.setText("正在获取" + providerMap.get(bestProvider) + "对象");
            mLocationDesc = String.format("定位类型为%s", providerMap.get(bestProvider));
            beginLocation(bestProvider); // 开始定位
            isLocationEnable = true;
        } else { // 定位提供者暂不可用
            textLocation.setText(providerMap.get(bestProvider) + "不可用");
            isLocationEnable = false;
        }
    }

    // 开始定位
    @SuppressLint("MissingPermission")
    private void beginLocation(String method) {
        // 设置定位管理器的位置变更监听器
        mLocationMgr.requestLocationUpdates(method, 300, 0, mLocationListener);
        // 获取最后一次成功定位的位置信息
        Location location = mLocationMgr.getLastKnownLocation(method);
        showLocation(location); // 显示定位结果文本
    }

    // 设置定位结果文本
    @SuppressLint("SetTextI18n")
    private void showLocation(Location location) {
        if (location != null) {
            // 创建一个根据经纬度查询详细地址的任务
//            GetAddressTask task = new GetAddressTask(getActivity(), location, address -> {
//                @SuppressLint("DefaultLocale") String desc = String.format("%s\n定位信息如下： " +
//                                "\n\t定位时间为%s，" + "\n\t经度为%f，纬度为%f，" +
//                                "\n\t高度为%d米，精度为%d米，" +
//                                "\n\t详细地址为%s。",
//                        mLocationDesc, DateUtil.formatDate(location.getTime()),
//                        location.getLongitude(), location.getLatitude(),
//                        Math.round(location.getAltitude()), Math.round(location.getAccuracy()),
//                        address);
//                textLocation.setText(desc);
//            });
            GetAddressTask task = new GetAddressTask(getActivity(), location, address -> {
                @SuppressLint("DefaultLocale") String desc = String.format(
                        "定位时间为%s，" +
                                "\n\t详细地址为%s。",
                        DateUtil.formatDate(location.getTime()),
                        address);
                textLocation.setText(desc);
            });
            task.start(); // 启动地址查询任务
        } else {
            textLocation.setText(mLocationDesc + "\n暂未获取到定位对象");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == GET_THUMBNAIL_CODE) {
                assert data != null;
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                imageShow.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationMgr.removeUpdates(mLocationListener); // 移除定位管理器的位置变更监听器
    }

    // 初始化套接字
    private void initSocket() {
        // 检查能否连上Socket服务器
        SocketUtil.checkSocketAvailable(getActivity(), Config.BASE_IP, Config.BASE_PORT);
        try {
            @SuppressLint("DefaultLocale") String uri = String.format("http://%s:%d/", Config.BASE_IP, Config.BASE_PORT);
            mSocket = IO.socket(uri); // 创建指定地址和端口的套接字实例
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        mSocket.connect(); // 建立Socket连接
    }

    private void sendImage() {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // 把位图数据压缩到字节数组输出流
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] byteArray = stream.toByteArray();
        byte[] buffer = new byte[BUFFER_SIZE];
        int position = 0;
        while(position < byteArray.length) {
            int size = Math.min(BUFFER_SIZE, byteArray.length - position);
            System.arraycopy(byteArray, position, buffer, 0, size);
            mSocket.emit("image", buffer);
            position += size;
        }
    }
}

