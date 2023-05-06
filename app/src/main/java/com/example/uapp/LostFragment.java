package com.example.uapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.params.BlackLevelPattern;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.uapp.item.LostItem;
import com.example.uapp.item.LostItemAdapter;
import com.example.uapp.thr.PostInfo;
import com.example.uapp.thr.UappService;

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

public class LostFragment extends Fragment {
    private List<LostItem> lostItemList = new ArrayList<>();
    private androidx.appcompat.widget.SearchView searchView;
    private String searchText;
    private String firstPostId = "";
    private String lastPostId = "";
    private Boolean onSearch = false;//表示当前是否在搜索中

    private UappService.Client UappServiceClient;
    private void initializeUappServiceClient() throws TException {
        // 创建TTransport对象
        TTransport transport = new TSocket("202.38.72.73", 7860);
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
        View view = inflater.inflate(R.layout.fragment_lost, container, false);

        //导航栏及菜单
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setTitle("失物登记列表");
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.lost_menu, menu);
                MenuItem searchItem = menu.findItem(R.id.action_search);
                searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
                // ************* 搜索 *************
                searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // 提交文本事件
                        searchText = query;
                        new searchTask().execute();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        // 在此处处理文本变化的事件
                        String text = newText;
                        // 处理文本
                        return false;
                    }
                });
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                onSearch = false;
                new LostFragment.getPostTask().execute();
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        // 设置下拉刷新的监听器
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                lostItemList.clear();
                new LostFragment.getPostTask().execute();
                swipeRefreshLayout.setRefreshing(false);
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
                Intent intent = new Intent(getActivity(), LostItemDetailActivity.class);
                // 添加要传递的数据
                LostItem lostItem = lostItemList.get(position);
                intent.putExtra("itemName", lostItem.getName());
                intent.putExtra("lostTime", lostItem.getLostTime());
                intent.putExtra("lostPos", lostItem.getPos());
                intent.putExtra("contact", lostItem.getPosterId());
                intent.putExtra("description", lostItem.getDesc());
                intent.putExtra("image",lostItem.getImagePath());
                // 启动活动
                startActivity(intent);
            }
        });



        // *********** 翻页 *************
        Button btn_next = view.findViewById(R.id.btn_next);
        Button btn_prev = view.findViewById(R.id.btn_prev);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lastPostId.isEmpty()){
                    Toast.makeText(getActivity(),"无法翻页！",Toast.LENGTH_SHORT).show();
                }else {
                    new turnPageTask().execute(true);
                }
            }
        });
        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firstPostId.isEmpty()){
                    Toast.makeText(getActivity(),"无法翻页！",Toast.LENGTH_SHORT).show();
                }else {
                    new turnPageTask().execute(false);
                }
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
                List<PostInfo> PostInfos = UappServiceClient.getPostBy10();
                for(PostInfo postInfo:PostInfos){
                    //判断图片是否存在,若不存在,则创建图片
                    String imageName = postInfo.getImage_name();
                    File file = new File(getContext().getFilesDir(), imageName);
                    if (!file.exists()) {
                        try {
                            byte [] image = postInfo.getItem_image();
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
                    LostItem lostItem = new LostItem(postInfo.getItem_type(),
                            file.getPath(),
                            new Date(postInfo.lost_time),
                            postInfo.getItem_position(),
                            postInfo.getStudent_id(),
                            new Date(postInfo.getDate()),
                            postInfo.isStatus(),
                            postInfo.getPost_id());
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
                    // List<PostInfo> PostInfos 为空
                    firstPostId = "";
                    lastPostId = "";
                }else {
                    firstPostId = lostItemList.get(0).getPostId();
                    lastPostId = lostItemList.get(lostItemList.size() - 1).getPostId();
                }
                Toast.makeText(getActivity(),"刷新成功！",
                        Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(),"刷新失败",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ************* 搜索线程 **************
    private class searchTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 创建Item对象
            try {
                initializeUappServiceClient();
                List<PostInfo> PostInfos = UappServiceClient.searchNext10(searchText,"",true,true);
                for(PostInfo postInfo:PostInfos){
                    //判断图片是否存在,若不存在,则创建图片
                    String imageName = postInfo.getImage_name();
                    File file = new File(getContext().getFilesDir(), imageName);
                    if (!file.exists()) {
                        try {
                            byte [] image = postInfo.getItem_image();
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
                    LostItem lostItem = new LostItem(postInfo.getItem_type(),
                            file.getPath(),
                            new Date(postInfo.lost_time),
                            postInfo.getItem_position(),
                            postInfo.getStudent_id(),
                            new Date(postInfo.getDate()),
                            postInfo.isStatus(),
                            postInfo.getPost_id());
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
                firstPostId = lostItemList.get(0).getPostId();
                lastPostId = lostItemList.get(lostItemList.size() - 1).getPostId();
                //设置搜索状态
                onSearch = true;
                Toast.makeText(getActivity(),"搜索成功！",
                        Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(),"搜索失败",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ************* 翻页线程 **************
    private class turnPageTask extends AsyncTask<Boolean, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            // 创建Item对象
            try {
                Boolean isNext = booleans[0];
                initializeUappServiceClient();
                List<PostInfo> PostInfos;
                if(isNext){//NextPage
                    PostInfos = UappServiceClient.searchNext10(searchText,lastPostId,onSearch, true);
                }
                else{//PrevPage
                    PostInfos = UappServiceClient.searchPrev10(searchText,firstPostId,onSearch,true);
                }
                if (PostInfos == null || PostInfos.isEmpty()) {
                    // List<PostInfo> PostInfos 为空
                    if(isNext){
                        Toast.makeText(getActivity(),"当前为最后一页！",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getActivity(),"当前为第一页！",Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
                for(PostInfo postInfo:PostInfos){
                    //判断图片是否存在,若不存在,则创建图片
                    String imageName = postInfo.getImage_name();
                    File file = new File(getContext().getFilesDir(), imageName);
                    if (!file.exists()) {
                        try {
                            byte [] image = postInfo.getItem_image();
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
                    LostItem lostItem = new LostItem(postInfo.getItem_type(),
                            file.getPath(),
                            new Date(postInfo.lost_time),
                            postInfo.getItem_position(),
                            postInfo.getStudent_id(),
                            new Date(postInfo.getDate()),
                            postInfo.isStatus(),
                            postInfo.getPost_id());
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
                if(lostItemList.isEmpty()){
                    firstPostId = "";
                    lastPostId = "";
                }else {
                    firstPostId = lostItemList.get(0).getPostId();
                    lastPostId = lostItemList.get(lostItemList.size() - 1).getPostId();
                }
                //设置搜索状态
                onSearch = true;
                Toast.makeText(getActivity(),"翻页成功！",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(),"翻页失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
