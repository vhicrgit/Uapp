package com.example.uapp;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.library.SuperTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.uapp.config.Config;
import com.example.uapp.item.LostItem;
import com.example.uapp.item.LostItemAdapter;
import com.example.uapp.thr.AbbrInfo;
import com.example.uapp.thr.LoginInfo;
import com.example.uapp.thr.RegisterInfo;
import com.example.uapp.thr.SetUserInfo;
import com.example.uapp.thr.UappService;
import com.example.uapp.user.AddrActivity;
import com.example.uapp.user.UsernameActivity;
import com.example.uapp.utils.AppearanceUtils;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class MineFragment extends Fragment {
    private View view = null;
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
    private SuperTextView tv_user_info;
    private SuperTextView tv_post_history;
    private SuperTextView tv_addr;
    private SuperTextView tv_setting;
    private SwitchCompat switchCompat;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Toolbar toolbar;

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
//        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        Log.d("=======debug_view_exist=======", ": "+(view != null));
        if (view == null) {
            // 创建新的视图
            view = inflater.inflate(R.layout.fragment_mine, container, false);
        }
        else{
            return view;
        }
//        ImageView headShot = view.findViewById(R.id.gray_headshot);
        tv_change_passward = view.findViewById(R.id.tv_change_passward);
        tv_user_info = view.findViewById(R.id.tv_user_info);
        tv_post_history = view.findViewById(R.id.tv_post_history);
        tv_addr = view.findViewById(R.id.tv_addr);
        tv_setting = view.findViewById(R.id.tv_setting);
        pref = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
        loggedIn = pref.getBoolean("loggedIn",false);
        tv_username = view.findViewById(R.id.tv_username);
        tv_sno = view.findViewById(R.id.tv_sno);
        if(loggedIn == false){
            tv_username.setText("未登录");
            tv_sno.setText("");
        } else {
            tv_username.setText(pref.getString("username","匿名"));
            tv_sno.setText(pref.getString("sno",""));
        }
//        headShot.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getActivity(), LoginActivity.class);
//                startActivityForResult(intent, LOG_IN);
//            }
//        });

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
        btn_log_out.setBackground(getResources().getDrawable(Config.themeColor_Button));
        btn_log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationDialog();
            }
        });

        //导航栏及菜单
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("我的信息");
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));
        //个人信息
        tv_user_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                startActivity(intent);
            }
        });

        //修改密码
        tv_change_passward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChangePasswardActivity.class);
                startActivity(intent);
            }
        });
        tv_addr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddrActivity.class);
                startActivity(intent);
            }
        });
        //关怀模式
        switchCompat = view.findViewById(R.id.modeSwitch);

        //常用位置
        tv_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });
        //发帖记录
        tv_post_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PostHistoryActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        switchCompat = view.findViewById(R.id.modeSwitch);
//        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {// 启用关怀模式
//                    editor = getActivity().getSharedPreferences("login_info",Context.MODE_PRIVATE).edit();
//                    editor.putBoolean("careMode",true);
//                    editor.apply();
//                    AppearanceUtils.increaseFontSize(view,1.25f);
//                } else {// 关闭关怀模式
//                    editor = getActivity().getSharedPreferences("login_info",Context.MODE_PRIVATE).edit();
//                    editor.putBoolean("careMode",false);
//                    editor.apply();
//                    AppearanceUtils.increaseFontSize(view,0.8f);
//                }
//            }
//        });
////        AppearanceUtils.increaseFontSize(view,1.25f);
//    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewTreeObserver observer = switchCompat.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(pref.getBoolean("careMode",false)){
                    //关怀模式
                    switchCompat.setChecked(true);
                    AppearanceUtils.increaseFontSize(view,1.25f);
                }
                switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {// 启用关怀模式
                            editor = getActivity().getSharedPreferences("login_info",Context.MODE_PRIVATE).edit();
                            editor.putBoolean("careMode",true);
                            editor.apply();
                            AppearanceUtils.increaseFontSize(view,1.25f);
                        } else {// 关闭关怀模式
                            editor = getActivity().getSharedPreferences("login_info",Context.MODE_PRIVATE).edit();
                            editor.putBoolean("careMode",false);
                            editor.apply();
                            AppearanceUtils.increaseFontSize(view,0.8f);
                        }
                    }
                });
                // 移除监听器，避免重复回调
                switchCompat.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
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
//                            showMsg("已获取权限");
                        } else {//申请失败
                            showMsg("权限未开启");
                        }
                    });
        } else {
            //Android6.0以下
//            showMsg("无需请求动态权限");
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
            closeItemServiceClient();
        }
    }
    private void showConfirmationDialog() {
        String message = "你确定要注销吗";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(message);
        // 设置"注销"两个字的样式
        int startIndex = message.indexOf("注销");
        int endIndex = startIndex + "注销".length();
        // 创建一个StyleSpan对象来设置字体样式（加粗）
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        // 创建一个ForegroundColorSpan对象来设置字体颜色（红色）
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
        // 将StyleSpan和ForegroundColorSpan应用于"注销"两个字
        spannableStringBuilder.setSpan(boldSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("确认");
        builder.setMessage(spannableStringBuilder);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击了确认按钮
                performAction();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击了取消按钮
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void performAction() {
        editor = getActivity().getSharedPreferences("login_info",Context.MODE_PRIVATE).edit();
        editor.putBoolean("loggedIn",false);
        editor.apply();
        tv_username.setText("未登录");
        tv_sno.setText("");
        Glide.with(getActivity()).load(R.mipmap.ic_launcher).apply(requestOptions).into(ivHead);
        String headShotPath = getActivity().getFilesDir() + File.separator + "headshot.jpg";
        File file = new File(headShotPath);
        boolean deleted = file.delete();
    }


    public void onResume() {
        super.onResume();
        loggedIn = pref.getBoolean("loggedIn",false);
        if(loggedIn == false){
            tv_username.setText("未登录");
            tv_sno.setText("");
        } else {
            tv_username.setText(pref.getString("username","匿名"));
            tv_sno.setText(pref.getString("sno",""));
        }
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));
        btn_log_out.setBackground(getResources().getDrawable(Config.themeColor_Button));
    }
}