package com.example.uapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.uapp.config.Config;
import com.example.uapp.item.LostItem;
import com.example.uapp.task.GetAddressTask;
import com.example.uapp.task.util.DateUtil;
import com.example.uapp.task.util.SocketUtil;
import com.example.uapp.thr.PostInfo;
import com.example.uapp.thr.RegisterInfo;
import com.example.uapp.thr.UappService;
import com.example.uapp.utils.AppearanceUtils;
import com.example.uapp.utils.BitmapUtils;
import com.example.uapp.utils.CameraUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.litepal.LitePal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import io.socket.client.IO;
import io.socket.client.Socket;

public class PostFoundActivity extends AppCompatActivity {
    //*********** item数据相关 ************
    private SharedPreferences pref;
    private String itemName;
    private int imageId;
    private java.util.Date lostTime;
    private String pos;
    private String desc;
    //帖子信息
    private String posterId;        //pk
    private java.util.Date postTime; //pk
    private boolean state;
    private byte[] img;
    private String thumbnailPath;
    private String imgPath = null;
    private String imgName = null;
    //*********** 照片相关 ************
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
    //启动相机标识
    public static final int TAKE_PHOTO = 1;
    //启动相册标识
    public static final int SELECT_PHOTO = 2;
    private String base64Pic;
    //拍照和相册获取图片的Bitmap
    private Bitmap orc_bitmap;
    //******************* 控件 *******************
    private Button btn_img;
    private ImageView iv_img;
    private EditText et_pos;
    private CheckBox cb_addr;
    //******************* 标识 *******************
    private Boolean imageExist = false;
    private UappService.Client UappServiceClient;

//    private final Map<String, String> providerMap = new HashMap<>();

//    private String mLocationDesc = ""; // 定位说明
//    private LocationManager mLocationMgr; // 声明一个定位管理器对象
//    private final Criteria criteria = new Criteria(); // 创建一个定位准则对象
//    private final Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象
//    private boolean isLocationEnable = false; // 定位服务是否可用
//
//    private Socket mSocket; // 声明一个套接字对象

    private void initializeUappServiceClient() throws TException {
        TTransport transport = new TSocket(getString(R.string.ip), getResources().getInteger(R.integer.port));
        TProtocol protocol = new TBinaryProtocol(transport);
        UappServiceClient = new UappService.Client(protocol);
        transport.open();
    }


//    private final LocationListener mLocationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(Location location) {
//            showLocation(location); // 显示定位结果文本
//        }
//
//        @Override
//        public void onProviderDisabled(String arg0) {
//        }
//
//        @Override
//        public void onProviderEnabled(String arg0) {
//        }
//
//        @Override
//        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
//        }
//    };

//    private final Runnable mRefresh = new Runnable() {
//        @Override
//        public void run() {
//            if (!isLocationEnable) {
//                initLocation(); // 初始化定位服务
//                mHandler.postDelayed(this, 1000);
//            }
//        }
//    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_found);
        pref = getSharedPreferences("login_info", MODE_PRIVATE);
        LitePal.getDatabase();
        EditText et_item_name = findViewById(R.id.et_type);
        et_pos = findViewById(R.id.et_pos);
        EditText et_desc = findViewById(R.id.et_desc);
        Button btn_post = findViewById(R.id.btn_post);
        EditText et_lost_time = findViewById(R.id.et_lost_time);
        cb_addr = findViewById(R.id.cb_addr);

        btn_img = findViewById(R.id.btn_img);
        iv_img = findViewById(R.id.iv_img);
        et_lost_time.setFocusable(false);

        //导航栏及菜单
        Toolbar toolbar = findViewById(R.id.toolbar_1);
        toolbar.setTitle("捡到失物上传");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));

        //自动设置时间
        java.util.Date currentDate = new java.util.Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(currentDate);
        et_lost_time.setText(formattedDate);

        //设置常用地点
        //TODO
        String commonAddr = pref.getString("addr","");
        cb_addr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if(!commonAddr.equals("")){
                        et_pos.setText(commonAddr);
                    }
                } else {
                    et_pos.setText("");
                }
            }
        });


        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkVersion();
                changeAvatar(view);
            }
        });

        //按下上传键
        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!imageExist) {
                    showMsg("请先上传一张图片！");
                }
                LitePal.getDatabase();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    //获取输入框中的日期、丢失物品名、地点
                    //用户名
                    SharedPreferences pref = getSharedPreferences("login_info", MODE_PRIVATE);
                    posterId = pref.getString("sno", "匿名");
                    //物品名
                    if (TextUtils.isEmpty(et_item_name.getText().toString())) {
                        Toast.makeText(PostFoundActivity.this, "丢失物品名不能为空！",
                                Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        itemName = et_item_name.getText().toString();
                    }
                    //地点
                    if (TextUtils.isEmpty(et_pos.getText().toString())) {
                        Toast.makeText(PostFoundActivity.this, "丢失地点不能为空！",
                                Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        pos = et_pos.getText().toString();
                    }
                    //时间
                    if (TextUtils.isEmpty(et_lost_time.getText().toString())) {//若未选择丢失时间，默认当天
                        lostTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
                    } else {
                        try {
                            lostTime = dateFormat.parse(et_lost_time.getText().toString());
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //描述
                    if (TextUtils.isEmpty(et_desc.getText().toString())) {
                        desc = "";
                    } else {
                        desc = et_desc.getText().toString();
                    }
                    //上传时间
                    postTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
                    //state
                    state = false;


//                    //设置数据，存入数据库
//                    LostItem lostItem = new LostItem(itemName,imgPath,postTime,pos);
//                    lostItem.setDesc(desc);
//                    lostItem.setPosterId(posterId);
//                    lostItem.setLostTime(lostTime);
//                    lostItem.setPostTime(postTime);
//                    lostItem.setState(state);
//                    lostItem.setImageId(R.drawable.img_2);
//                    lostItem.setPostId(postTime.toString() + posterId);
//                    lostItem.save();

                    Log.d("MyApp", imgPath);

                    new PostFoundActivity.PostFoundTask().execute();
                }

            }
        });
        //用户点击EditText框，弹出calendar进行日期选择
        et_lost_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取当前日期
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                // 创建DatePickerDialog实例
                DatePickerDialog datePickerDialog = new DatePickerDialog(PostFoundActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // 处理用户选择的日期
                                String date = year + "-" + (month + 1) + "-" + dayOfMonth;
                                et_lost_time.setText(date);
                            }
                        }, year, month, dayOfMonth);
                // 显示DatePickerDialog
                datePickerDialog.show();
            }
        });
    }

    // ************* 与服务器交互的新线程 *************
    private class PostFoundTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // 创建Item对象
            try {
                Boolean reactFromServer;
                initializeUappServiceClient();
                SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                        "yyyy_MM_dd_HH_mm_ss_");
                PostInfo lostItemInfo = new PostInfo();
                lostItemInfo.setItem_type(itemName);
                lostItemInfo.setFor_lost_item(false);
                lostItemInfo.setStudent_id(posterId);
                lostItemInfo.setItem_position(pos);
                lostItemInfo.setItem_desc(desc);
                lostItemInfo.setDate(postTime.getTime());
                lostItemInfo.setStatus(state);
                lostItemInfo.setLost_time(lostTime.getTime());
                lostItemInfo.setImage_name(imgName);
                lostItemInfo.setPost_id(timeStampFormat.format(postTime) + posterId);

                Log.d("=== itemName ===", itemName);
                Log.d("=== posterId ===", posterId);
                Log.d("=== setItem_desc ===", desc);
                Log.d("=== postTime ===", postTime.toString());
                Log.d("=== lostTime ===", lostTime.toString());
                Log.d("=== imgName ===", imgName);
//                Log.d("=== postId ===", postId);
                // 读取完整图片
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Path imagePath = Paths.get(imgPath);
                    byte[] imageBytes = Files.readAllBytes(imagePath);
                    lostItemInfo.setItem_image(imageBytes);
                }
                // 读取缩略图
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Path imagePath = Paths.get(thumbnailPath);
                    byte[] imageBytes = Files.readAllBytes(imagePath);
                    lostItemInfo.setThumbnail(imageBytes);
                }
                reactFromServer = UappServiceClient.uploadPost(lostItemInfo);
                return reactFromServer;
            } catch (TException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(PostFoundActivity.this, "上传成功！",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(PostFoundActivity.this, "上传失败",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    //************** 辅助函数 ***************
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查版本
     */
    private void checkVersion() {
        //Android6.0及以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rxPermissions = new RxPermissions(this);
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
        SharedPreferences pref = getSharedPreferences("login_info", MODE_PRIVATE);
        posterId = pref.getString("sno", "null");
        SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                "yyyy_MM_dd_HH_mm_ss_");
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        String filename = timeStampFormat.format(new java.util.Date());
        outputImagePath = new File(storageDir,
                filename + posterId + ".jpg");
        imgName = filename + posterId + ".jpg";
        Intent takePhotoIntent = CameraUtils.getTakePhotoIntent(this, outputImagePath);

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

        Intent selectPhotoIntent = CameraUtils.getSelectPhotoIntent();
        startActivityForResult(selectPhotoIntent, SELECT_PHOTO);
    }

    /**
     * 通过图片路径显示图片
     */
    private void displayImage(String imagePath) {
        if (!TextUtils.isEmpty(imagePath)) {
            //显示图片
            Glide.with(this).load(imagePath).into(iv_img);
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
                if (resultCode == RESULT_OK) {
                    imgPath = outputImagePath.getAbsolutePath();
                    File file = new File(imgPath);
                    Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                    if (bitmap != null) {
                        // 压缩图片
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ByteArrayOutputStream mid = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, mid);
                        int size = mid.toByteArray().length / (1024 * 1024);
                        if (size > 10) {//压缩到512kb以内
                            showMsg("图片过大");
                        } else if (size > 5) {//压缩到512kb以内
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
                        } else {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                        }
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        try {
                            // 覆盖原文件
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(byteArray);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //显示图片
                    displayImage(imgPath);
                    //设置标识，用户已选择图片
                    imageExist = true;
                    //存储缩略图
                    saveThumbnail();
                }
                break;
            //打开相册后返回
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        //4.4及以上系统使用这个方法处理图片
                        imgPath = CameraUtils.getImageOnKitKatPath(data, this);
                    } else {
                        imgPath = CameraUtils.getImageBeforeKitKatPath(data, this);
                    }
                    //保存图片
                    saveSelectedImage(imgPath);
                    //显示图片
                    displayImage(imgPath);
                    //设置标识，用户已选择图片
                    imageExist = true;
                    //存储缩略图
                    saveThumbnail();
                }
                break;
            default:
                break;
        }
    }

    public void changeAvatar(View view) {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomView = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
        bottomSheetDialog.setContentView(bottomView);
        bottomSheetDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundColor(Color.TRANSPARENT);
        TextView tvTakePictures = bottomView.findViewById(R.id.tv_take_pictures);
        TextView tvOpenAlbum = bottomView.findViewById(R.id.tv_open_album);
        TextView tvCancel = bottomView.findViewById(R.id.tv_cancel);

        //拍照
        tvTakePictures.setOnClickListener(v -> {
            takePhoto();
            bottomSheetDialog.cancel();
        });
        //打开相册
        tvOpenAlbum.setOnClickListener(v -> {
            openAlbum();
            bottomSheetDialog.cancel();
        });
        //取消
        tvCancel.setOnClickListener(v -> {
            bottomSheetDialog.cancel();
        });
        bottomSheetDialog.show();
    }


    public void saveSelectedImage(String imageUrl) {
        File file = new File(imageUrl);
        SharedPreferences pref = getSharedPreferences("login_info", MODE_PRIVATE);
        posterId = pref.getString("sno", "null");
        SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                "yyyy_MM_dd_HH_mm_ss_");
        String filename = timeStampFormat.format(new java.util.Date());
        try {
            InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(file));
            // 将输入流转换为Bitmap对象
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // 获取应用程序私有文件夹
            File privateDir = getApplicationContext().getFilesDir();
            // 创建一个新的File对象，表示要保存的文件
            File savedFile = new File(privateDir, filename + posterId + ".jpg");
            // 使用FileOutputStream和compress()方法将Bitmap对象写入文件
            imgPath = privateDir + "/" + filename + posterId + ".jpg";
            imgName = filename + posterId + ".jpg";
            FileOutputStream outputStream = new FileOutputStream(savedFile);

            ByteArrayOutputStream mid = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, mid);
            float size = mid.toByteArray().length / (1024 * 1024);
            int quality = (int) (100 / size);
            if (size > 10) {
                showMsg("图片过大");
            } else if (size > 5) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, outputStream);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream);
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private void saveThumbnail() {
        // 原始图片路径
        String imagePath = imgPath;

        // 定义目标宽度和高度
        int targetWidth = 128; // 目标宽度
        int targetHeight = 128; // 目标高度

        // 读取原始图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // 计算缩放比例
        int scaleFactor = Math.min(options.outWidth / targetWidth, options.outHeight / targetHeight);

        // 设置缩放比例并加载图片
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

        String fileName;
        String parentPath;
        File ff = new File(imagePath);
        parentPath = ff.getParent();
        fileName = ff.getName();
        // 去掉文件后缀
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            fileName = fileName.substring(0, dotIndex);
        }

        // 保存缩小后的图片
        thumbnailPath = parentPath + File.separator + fileName + "_thumbnail.jpg";  // 构建新图片的路径

        try {
            FileOutputStream outputStream = new FileOutputStream(thumbnailPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        mHandler.removeCallbacks(mRefresh); // 移除定位刷新任务
//        initLocation(); // 初始化定位服务
//        mHandler.postDelayed(mRefresh, 100); // 延迟100毫秒启动定位刷新任务
//        // 初始化套接字
//        initSocket();
//    }
//
//    @SuppressLint("SetTextI18n")
//    private void initLocation() {
//        // 从系统服务中获取定位管理器
//        mLocationMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 设置定位精确度
//        criteria.setAltitudeRequired(true); // 设置是否需要海拔信息
//        criteria.setBearingRequired(true); // 设置是否需要方位信息
//        criteria.setCostAllowed(true); // 设置是否允许运营商收费
//        criteria.setPowerRequirement(Criteria.POWER_LOW); // 设置对电源的需求
//        // 获取定位管理器的最佳定位提供者
//        String bestProvider = mLocationMgr.getBestProvider(criteria, true);
//
//        if (bestProvider != null && mLocationMgr.isProviderEnabled(bestProvider)) { // 定位提供者当前可用
//            et_pos.setText("正在获取" + providerMap.get(bestProvider) + "对象");
//            mLocationDesc = String.format("定位类型为%s", providerMap.get(bestProvider));
//            beginLocation(bestProvider); // 开始定位
//            isLocationEnable = true;
//        } else { // 定位提供者暂不可用
//            et_pos.setText(providerMap.get(bestProvider) + "不可用");
//            isLocationEnable = false;
//        }
//    }

//    private void beginLocation(String method) {
//        // 设置定位管理器的位置变更监听器
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mLocationMgr.requestLocationUpdates(method, 300, 0, mLocationListener);
//        // 获取最后一次成功定位的位置信息
//        Location location = mLocationMgr.getLastKnownLocation(method);
//        showLocation(location); // 显示定位结果文本
//    }
//
//    // 设置定位结果文本
//    @SuppressLint("SetTextI18n")
//    private void showLocation(Location location) {
//        if (location != null) {
//            // 创建一个根据经纬度查询详细地址的任务
////            GetAddressTask task = new GetAddressTask(getActivity(), location, address -> {
////                @SuppressLint("DefaultLocale") String desc = String.format("%s\n定位信息如下： " +
////                                "\n\t定位时间为%s，" + "\n\t经度为%f，纬度为%f，" +
////                                "\n\t高度为%d米，精度为%d米，" +
////                                "\n\t详细地址为%s。",
////                        mLocationDesc, DateUtil.formatDate(location.getTime()),
////                        location.getLongitude(), location.getLatitude(),
////                        Math.round(location.getAltitude()), Math.round(location.getAccuracy()),
////                        address);
////                textLocation.setText(desc);
////            });
//            GetAddressTask task = new GetAddressTask(this, location, address -> {
//                @SuppressLint("DefaultLocale") String desc = String.format(
//                        "定位时间为%s，" +
//                                "\n\t详细地址为%s。",
//                        DateUtil.formatDate(location.getTime()),
//                        address);
//                et_pos.setText(desc);
//            });
//            task.start(); // 启动地址查询任务
//        } else {
//            et_pos.setText(mLocationDesc + "\n暂未获取到定位对象");
//        }
//    }

    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mLocationMgr.removeUpdates(mLocationListener); // 移除定位管理器的位置变更监听器
//    }

//    // 初始化套接字
//    private void initSocket() {
//        // 检查能否连上Socket服务器
//        SocketUtil.checkSocketAvailable(this, Config.BASE_IP, Config.BASE_PORT);
//        try {
//            @SuppressLint("DefaultLocale") String uri = String.format("http://%s:%d/", Config.BASE_IP, Config.BASE_PORT);
//            mSocket = IO.socket(uri); // 创建指定地址和端口的套接字实例
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//        mSocket.connect(); // 建立Socket连接
//    }

    protected void onStart() {
        super.onStart();
        ViewGroup rootView = findViewById(android.R.id.content);
        ViewTreeObserver observer = rootView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                pref = getSharedPreferences("login_info", Context.MODE_PRIVATE);
                if(pref.getBoolean("careMode",false)){
                    //关怀模式
                    AppearanceUtils.increaseFontSize(rootView,1.25f);
                }
                // 移除监听器，避免重复回调
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {finish();}
        return super.onOptionsItemSelected(item);
    }
}
