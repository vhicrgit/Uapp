package com.example.uapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.uapp.utils.PermissionUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private BottomNavigationView mNavigationView;

    private FragmentManager mFragmentManager;

    private Fragment[] fragments;
    private int lastFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermissionCheckAndRequest();
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        // 获取navController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        // 通过setupWithController将底部导航和导航控制器进行绑定
        NavigationUI.setupWithNavController(bottomNavigation, navController);
        bottomNavigation.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);



//        bottomNavigation = findViewById(R.id.bottom_navigation);
//        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.home_fragment:
//                    // 设置图标
//                    item.setIcon(R.drawable.ic_home);
//                    // 处理导航项1的逻辑
//                    return true;
//                case R.id.lost_fragment:
//                    // 设置图标
//                    item.setIcon(R.drawable.ic_home);
//                    // 处理导航项2的逻辑
//                    return true;
//                case R.id.found_fragment:
//                    // 设置图标
////                    item.setIcon(R.drawable.ic_item3_selected);
//                    // 处理导航项3的逻辑
//                    return true;
//                case R.id.mine_fragment:
//                    // 设置图标
////                    item.setIcon(R.drawable.ic_item3_selected);
//                    // 处理导航项3的逻辑
//                    return true;
//            }
//            return false;
//        });
    }

    private void initPermissionCheckAndRequest() {
        PermissionUtil.checkPermission(this, new String[] { android.Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要允许照相机权限才能拍照", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initFragment() {
        HomeFragment mHomeFragment = new HomeFragment();
        LostFragment mPlanFragment = new LostFragment();
        FoundFragment mGameFragment = new FoundFragment();
        MineFragment mSettingFragment = new MineFragment();
        fragments = new Fragment[]{mHomeFragment, mPlanFragment, mGameFragment, mSettingFragment};
        mFragmentManager = getSupportFragmentManager();
        //默认显示HomeFragment
        mFragmentManager.beginTransaction()
                .replace(R.id.bottom_navigation, mHomeFragment)
                .show(mHomeFragment)
                .commit();
    }

    private void initListener() {
        mNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_fragment:
                        if (lastFragment != 0) {
                            MainActivity.this.switchFragment(lastFragment, 0);
                            lastFragment = 0;
                        }
                        return true;
                    case R.id.lost_fragment:
                        if (lastFragment != 1) {
                            MainActivity.this.switchFragment(lastFragment, 1);
                            lastFragment = 1;
                        }
                        return true;
                    case R.id.found_fragment:
                        if (lastFragment != 2) {
                            MainActivity.this.switchFragment(lastFragment, 2);
                            lastFragment = 2;
                        }
                        return true;
                    case R.id.mine_fragment:
                        if (lastFragment != 3) {
                            MainActivity.this.switchFragment(lastFragment, 3);
                            lastFragment = 3;
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void switchFragment(int lastFragment, int index) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.hide(fragments[lastFragment]);
        if (!fragments[index].isAdded()){
            transaction.add(R.id.bottom_navigation,fragments[index]);
        }
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }

}