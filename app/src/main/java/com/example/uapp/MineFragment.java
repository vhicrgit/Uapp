package com.example.uapp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.library.SuperTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.uapp.item.LostItem;
import com.example.uapp.item.LostItemAdapter;
import com.example.uapp.thr.AbbrInfo;
import com.example.uapp.thr.LoginInfo;
import com.example.uapp.thr.RegisterInfo;
import com.example.uapp.thr.SetUserInfo;
import com.example.uapp.thr.UappService;
import com.example.uapp.utils.BitmapUtils;
import com.example.uapp.utils.CameraUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class MineFragment extends Fragment {
    private static final int LOG_IN = 3;
    private boolean loggedIn;
    //权限请求
    private RxPermissions rxPermissions;
    //是否拥有权限
    private boolean hasPermissions = false;
    //底部弹窗
    private BottomSheetDialog bottomSheetDialog;
    //弹窗视图
    private View bottomView;
    //存储拍完照后的图片
    private File outputImagePath;
    private String headShotPath;
    //启动相机标识
    public static final int TAKE_PHOTO = 1;
    //启动相册标识
    public static final int SELECT_PHOTO = 2;
    //图片控件
    private ShapeableImageView ivHead;
    //Base64
    private String base64Pic;
    //拍照和相册获取图片的Bitmap
    private Bitmap orc_bitmap;

    //Glide请求图片选项配置
    private RequestOptions requestOptions = RequestOptions.circleCropTransform()
            .diskCacheStrategy(DiskCacheStrategy.NONE)//不做磁盘缓存
            .skipMemoryCache(true);//不做内存缓存

    //控件
    private Button btn_log_out;
    private TextView tv_sno;
    private TextView tv_username;
    private SuperTextView tv_change_passward;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        ImageView headShot = view.findViewById(R.id.gray_headshot);
        SharedPreferences pref = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
        loggedIn = pref.getBoolean("loggedIn",false);
        tv_username = view.findViewById(R.id.tv_username);
        tv_sno = view.findViewById(R.id.tv_sno);
        if(loggedIn == false){
            tv_username.setText("未登录");
            tv_sno.setText("");
        } else {
            SharedPreferences userInfo = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
            tv_username.setText(userInfo.getString("username","匿名"));
            tv_sno.setText(userInfo.getString("sno",""));
        }
        headShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(intent, LOG_IN);
//                SharedPreferences pref = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
//                loggedIn = pref.getBoolean("loggedIn",false);
//                if(loggedIn == false){
//                    tv_username.setText("未登录");
//                    tv_sno.setText("");
//                } else {
//                    SharedPreferences userInfo = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
//                    tv_username.setText(userInfo.getString("username","匿名"));
//                    tv_sno.setText(userInfo.getString("sno",""));
//                }
            }
        });

        //头像
        ivHead = view.findViewById(R.id.iv_head);
        String head_shot_path = getContext().getFilesDir() + File.separator + "headshot.jpg";
        Log.d("=======debug_head_shot_path=======", ": "+head_shot_path);
        File file = new File(head_shot_path);
        if(file.exists() && loggedIn){
            displayImage(head_shot_path);
        }
        checkVersion();
        ShapeableImageView v_head = view.findViewById(R.id.iv_head);
        v_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断是否登录
                loggedIn = pref.getBoolean("loggedIn",false);
                if(loggedIn){
                    changeAvatar(view);
                }else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent, LOG_IN);
                }
            }
        });

        //注销
        btn_log_out = view.findViewById(R.id.btn_logout);
        btn_log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("login_info",Context.MODE_PRIVATE).edit();
                editor.putBoolean("loggedIn",false);
                editor.apply();
                tv_username.setText("未登录");
                tv_sno.setText("");
                Glide.with(getActivity()).load(R.mipmap.ic_launcher).apply(requestOptions).into(ivHead);
                String headShotPath = getActivity().getFilesDir() + File.separator + "headshot.jpg";
                File file = new File(headShotPath);
                boolean deleted = file.delete();
            }
        });

        //导航栏及菜单
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("我的信息");
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        //修改密码
        tv_change_passward = view.findViewById(R.id.tv_change_passward);
        tv_change_passward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChangePasswardActivity.class);
                startActivity(intent);
            }
        });

        //个人信息
        //TODO

        //常用位置
        //TODO

        return view;
    }

    private void showMsg(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
    /**
     * 检查版本
     */
    private void checkVersion() {
        //Android6.0及以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rxPermissions = new RxPermissions(getActivity());
            //权限请求
            rxPermissions.request(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        if (granted) {//申请成功
                            hasPermissions = true;
                            showMsg("已获取权限");
                        } else {//申请失败
                            showMsg("权限未开启");
                        }
                    });
        } else {
            //Android6.0以下
            showMsg("无需请求动态权限");
        }
    }



    /**
     * 拍照
     */
    private void takePhoto() {
        if (!hasPermissions) {
            showMsg("未获取到权限");
            checkVersion();
            return;
        }
        SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                "yyyy_MM_dd_HH_mm_ss");
        String filename = timeStampFormat.format(new Date());
        outputImagePath = new File(getContext().getExternalCacheDir(),
                filename + ".jpg");
        Intent takePhotoIntent = CameraUtils.getTakePhotoIntent(getActivity(), outputImagePath);
        // 开启一个带有返回值的Activity，请求码为TAKE_PHOTO
        startActivityForResult(takePhotoIntent, TAKE_PHOTO);
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        if (!hasPermissions) {
            showMsg("未获取到权限");
            checkVersion();
            return;
        }
        startActivityForResult(CameraUtils.getSelectPhotoIntent(), SELECT_PHOTO);
    }

    /**
     * 通过图片路径显示图片
     */
    private void displayImage(String imagePath) {

        if (!TextUtils.isEmpty(imagePath)) {
            //显示图片
            Glide.with(this).load(imagePath).apply(requestOptions).into(ivHead);
            //压缩图片
            orc_bitmap = CameraUtils.compression(BitmapFactory.decodeFile(imagePath));
            //转Base64
            base64Pic = BitmapUtils.bitmapToBase64(orc_bitmap);

        } else {
            showMsg("图片获取失败");
        }
    }


    /**
     * 返回到Activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //拍照后返回
            case TAKE_PHOTO:
                if (resultCode == getActivity().RESULT_OK) {
                    headShotPath = outputImagePath.getAbsolutePath();
                    //修改用户头像
                    new setUserInfoTask().execute();
                   //显示图片
                    displayImage(outputImagePath.getAbsolutePath());
                }
                break;
            //打开相册后返回
            case SELECT_PHOTO:
                if (resultCode == getActivity().RESULT_OK) {
                    String imagePath = null;
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        //4.4及以上系统使用这个方法处理图片
                        imagePath = CameraUtils.getImageOnKitKatPath(data, getActivity());
                    } else {
                        imagePath = CameraUtils.getImageBeforeKitKatPath(data, getActivity());
                    }
                    headShotPath = imagePath;
                    //修改用户头像
                    new setUserInfoTask().execute();
                    //显示图片
                    displayImage(imagePath);
                }
                break;
            case LOG_IN:
                Log.d("=======debug_LOG_IN=======", "LOG_IN");
                SharedPreferences userInfo = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
                loggedIn = userInfo.getBoolean("loggedIn",false);
                Log.d("=======debug_LOG_IN=======", "loggedIn = " + loggedIn);
                if(loggedIn == true){
                    tv_username.setText(userInfo.getString("username","匿名"));
                    tv_sno.setText(userInfo.getString("sno",""));
                }
                break;
            default:
                break;
        }
    }


    public void changeAvatar(View view) {
        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomView = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
        bottomSheetDialog.setContentView(bottomView);
        bottomSheetDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundColor(Color.TRANSPARENT);
        TextView tvTakePictures = bottomView.findViewById(R.id.tv_take_pictures);
        TextView tvOpenAlbum = bottomView.findViewById(R.id.tv_open_album);
        TextView tvCancel = bottomView.findViewById(R.id.tv_cancel);

        //拍照
        tvTakePictures.setOnClickListener(v -> {
            takePhoto();
            showMsg("拍照");
            bottomSheetDialog.cancel();
        });
        //打开相册
        tvOpenAlbum.setOnClickListener(v -> {
            openAlbum();
            showMsg("打开相册");
            bottomSheetDialog.cancel();
        });
        //取消
        tvCancel.setOnClickListener(v -> {
            bottomSheetDialog.cancel();
        });
        bottomSheetDialog.show();
    }



    private class setUserInfoTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 创建Item对象
            try {
                Boolean res;
                initializeUappServiceClient();
                SetUserInfo userInfo = new SetUserInfo();
                userInfo.setEmail("");
                userInfo.setContact("");
                userInfo.setPassword("");
                SharedPreferences sno = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
                userInfo.setStudent_id(sno.getString("sno","null"));
                userInfo.setUsername("");
                userInfo.setWhich(6);


                File file = new File(headShotPath);
                File file_des = new File(getContext().getFilesDir(), "headshot.jpg");
                int targetWidth = 128; // 目标宽度
                int targetHeight = 128; // 目标高度
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(headShotPath, options);
                // 计算缩放比例
                int scaleFactor = Math.min(options.outWidth / targetWidth, options.outHeight / targetHeight);
                // 设置缩放比例并加载图片
                options.inJustDecodeBounds = false;
                options.inSampleSize = scaleFactor;
                Bitmap bitmap = BitmapFactory.decodeFile(headShotPath, options);
                if (bitmap != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ByteArrayOutputStream mid = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    userInfo.setHeadshot(byteArray);
                    res = UappServiceClient.setUserInfo(userInfo);
                    if(!res){
                        return false;
                    }
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file_des);
                        fileOutputStream.write(byteArray);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (TException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(result){
                displayImage(getContext().getFilesDir() + File.separator + "headshot.jpg");
                Toast.makeText(getActivity(),"新头像设置成功！",
                        Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(),"头像设置失败",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private class getUserInfoTask extends AsyncTask<Void, Void, Boolean> {
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            try {
//                Boolean logInSuccess;
//                initializeUappServiceClient();
//                SharedPreferences pref = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
//                String sno = pref.getString("sno","匿名");
//                RegisterInfo registerInfo = new RegisterInfo();
//                registerInfo = UappServiceClient.getUserInfo(sno);
//
//                SharedPreferences.Editor editor = getActivity().getSharedPreferences("login_info",Context.MODE_PRIVATE).edit();
//                editor.putString("sno",sno);
//                editor.putString("username",username);
//                editor.putString("passward",passward);
//                editor.putString("email",email);
//
//                return true;
//            } catch (TException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result){
//            if(result){
//                Toast.makeText(LoginActivity.this,"登陆成功！",
//                        Toast.LENGTH_SHORT).show();
//                passward = et_passward.getText().toString();
//                loggedIn = true;
//                SharedPreferences.Editor editor = getSharedPreferences("login_info",MODE_PRIVATE).edit();
//                editor.putString("sno",sno);
//                editor.putString("username",username);
//                editor.putString("passward",passward);
//                editor.putString("email",email);
//                editor.putBoolean("loggedIn",loggedIn);
//                editor.apply();
//                finish();
//            }
//            else{
//                Toast.makeText(getActivity(),"获取用户信息失败",
//                        Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

}