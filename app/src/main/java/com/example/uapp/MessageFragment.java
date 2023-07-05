package com.example.uapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.params.BlackLevelPattern;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.controls.actions.BooleanAction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.uapp.config.Config;
import com.example.uapp.item.LostItem;
import com.example.uapp.item.LostItemAdapter;
import com.example.uapp.thr.AbbrInfo;
import com.example.uapp.thr.PostInfo;
import com.example.uapp.thr.UappService;
import com.example.uapp.utils.AppearanceUtils;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.litepal.LitePal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MessageFragment extends Fragment {
    private List<LostItem> lostItemList = new ArrayList<>();
    private String firstPostId = "";
    private String lastPostId = "";
    private String user_sno = "";
    private Boolean logged_in = false;
    private long debug_lost_time;
    private long debug_post_time;
    private SharedPreferences pref;

    private SwipeRefreshLayout swipeRefreshLayout;

    private UappService.Client UappServiceClient;
    private void initializeUappServiceClient() throws TException {
        // 创建TTransport对象
        TTransport transport = new TSocket(getString(R.string.ip), getResources().getInteger(R.integer.port));
        // 创建TProtocol对象
        TProtocol protocol = new TBinaryProtocol(transport);
        // 创建ItemService.Client对象
        UappServiceClient = new UappService.Client(protocol);
        // 打开transport
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
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        //导航栏及菜单
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setTitle("疑似失物");
        toolbar.setBackgroundColor(getResources().getColor(Config.themeColor));
        toolbar.setTitleTextColor(getResources().getColor(Config.themeColor_Text));
//        requireActivity().addMenuProvider(new MenuProvider() {
//            @Override
//            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
//                menuInflater.inflate(R.menu.lost_menu, menu);
//            }
//            @Override
//            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
//                new MessageFragment.getPostTask().execute();
//                return true;
//            }
//        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        // 设置下拉刷新的监听器
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                lostItemList.clear();
                new MessageFragment.getPostTask().execute();
            }
        });

        try {
            initLostItem();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        LostItemAdapter adapter = new LostItemAdapter(getActivity(),
                R.layout.lost_item,lostItemList);
        ListView listview = view.findViewById(R.id.listview);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 处理ListView的点击事件
                // position 是所选中的项目在ListView中的位置
                Intent intent = new Intent(getActivity(), MessageDetailActivity.class);
                // 添加要传递的数据
                LostItem lostItem = lostItemList.get(position);
                intent.putExtra("itemName", lostItem.getName());
                intent.putExtra("postId",lostItem.getPostId());
                intent.putExtra("lostTime", lostItem.getLostTime().getTime());
                intent.putExtra("lostPos", lostItem.getPos());
                intent.putExtra("contact", lostItem.getPosterId());
                intent.putExtra("description", lostItem.getDesc());
                intent.putExtra("imagePath", lostItem.getImagePath());
                intent.putExtra("sno", lostItem.getPosterId());
                intent.putExtra("contact", lostItem.getContact());
                intent.putExtra("status", lostItem.getState());
                // 启动活动
                startActivity(intent);
            }
        });


        return view;
    }

    private void initLostItem() throws ParseException {
        List<LostItem> lostItems = LitePal.findAll(LostItem.class);
        for(int i=0; i<1; i++){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                for(LostItem lostItem : lostItems) {
//                    LostItem bike = new LostItem(lostItem.getName(), lostItem.getImageId(), lostItem.getLostTime(), lostItem.getPos());
                    lostItemList.add(lostItem);
                }
            }
        }
    }


    // ************* 下拉刷新线程 **************
    private class getPostTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 创建Item对象
            try {
                initializeUappServiceClient();
                pref = getActivity().getSharedPreferences("login_info", Context.MODE_PRIVATE);
                user_sno = pref.getString("sno","");
                logged_in = pref.getBoolean("loggedIn",false);
                if(!logged_in)
                {
                    return false;
                }
                List<AbbrInfo> AbbrInfos = UappServiceClient.getSimilarPost(user_sno);
                for(AbbrInfo abbrInfo:AbbrInfos){
                    //判断图片是否存在,若不存在,则创建图片
                    String imageName = abbrInfo.getImage_name();
                    File file_full = new File(getContext().getFilesDir(), imageName);
                    // 去掉文件后缀
                    int dotIndex = imageName.lastIndexOf(".");
                    if (dotIndex > 0 && dotIndex < imageName.length() - 1) {
                        imageName = imageName.substring(0, dotIndex);
                    }
                    imageName = imageName + "_thumbnail.jpg";
                    File file = new File(getContext().getFilesDir(), imageName);
                    if (!file.exists()) {
                        try {
                            byte [] image = abbrInfo.getThumbnail();
                            // 将 byte[] 数据转换为 Bitmap
                            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                            // 将 Bitmap 保存为文件
                            FileOutputStream outputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Date lost_time = new Date(abbrInfo.getLost_time());
                    Date post_time = new Date(abbrInfo.getDate());
                    debug_lost_time = abbrInfo.getLost_time();
                    debug_post_time = abbrInfo.getDate();
                    LostItem lostItem = new LostItem(abbrInfo.getItem_type(),
                            file_full.getPath(),
                            lost_time,
                            abbrInfo.getItem_position(),
                            abbrInfo.getStudent_id(),
                            post_time,
                            abbrInfo.isStatus(),
                            abbrInfo.getPost_id(),
                            abbrInfo.getItem_desc(),
                            abbrInfo.getContact()
                    );
                    lostItemList.add(lostItem);
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
                //重新载入lostItemList
                LostItemAdapter adapter = new LostItemAdapter(getActivity(),
                        R.layout.lost_item,lostItemList);
                ListView listview = getView().findViewById(R.id.listview);
                listview.setAdapter(adapter);
                //获取首尾帖子id
                if (lostItemList == null || lostItemList.isEmpty()) {
                    // List<AbbrInfo> AbbrInfos 为空
                    firstPostId = "";
                    lastPostId = "";
                }else {
                    firstPostId = lostItemList.get(0).getPostId();
                    lastPostId = lostItemList.get(lostItemList.size() - 1).getPostId();
                }
                Toast.makeText(getActivity(),"刷新成功！",
                        Toast.LENGTH_SHORT).show();
                Log.d("========== debug_lost_time ==========", Long.toString(debug_lost_time));
                Log.d("========== debug_post_time ==========", Long.toString(debug_post_time));
            }
            else{
                Toast.makeText(getActivity(),"刷新失败",
                        Toast.LENGTH_SHORT).show();
            }
            closeItemServiceClient();
            swipeRefreshLayout.setRefreshing(false);
        }
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
