package com.example.uapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.uapp.util.PermissionUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermissionCheckAndRequest();
        bottomNavigation = findViewById(R.id.bottom_navigation);
        // 获取navController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        // 通过setupWithController将底部导航和导航控制器进行绑定
        NavigationUI.setupWithNavController(bottomNavigation, navController);
        bottomNavigation.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
    }

    private void initPermissionCheckAndRequest() {
        PermissionUtil.checkPermission(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        PermissionUtil.checkPermission(this, new String[] { android.Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // requestCode不能为负数，也不能大于2的16次方即65536
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要允许定位权限才能开始定位", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要允许照相机权限才能拍照", Toast.LENGTH_SHORT).show();
            }
        }
    }
}