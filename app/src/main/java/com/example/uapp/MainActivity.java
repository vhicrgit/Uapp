package com.example.uapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Wallpaper> wallpaperList = new ArrayList<>();
    private String[] wallpapersNames = {
            "lovewar",
            "inoue",
            "chisato",
            "lustrous",
            "date",
            "asuka",
            "rain",
            "witch"
    };

    private int[] wallpapersIds = {
            R.drawable.img_1,
            R.drawable.img_2,
            R.drawable.img_3,
            R.drawable.img_4,
            R.drawable.img_5,
            R.drawable.img_6,
            R.drawable.img_7,
            R.drawable.img_8
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWallpapers();
        WallpaperAdapter adapter = new WallpaperAdapter(MainActivity.this, R.layout.wallpaper_item, wallpaperList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id) {
                Wallpaper wallpaper = wallpaperList.get(position);
                Toast.makeText(MainActivity.this, wallpaper.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class Wallpaper {
        private String name;
        private int imageId;
        public Wallpaper(String name, int imageId) {
            this.name = name;
            this.imageId = imageId;
        }

        public String getName() {
            return name;
        }
        public int getImageId() {
            return imageId;
        }
    }

    public class WallpaperAdapter extends ArrayAdapter<Wallpaper> {
        private int resourceId;
        public  WallpaperAdapter(Context context, int textViewResourceId, List<Wallpaper> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Wallpaper wallpaper = getItem(position); // 获取当前项的wallpaper实例
            View view ;
            ViewHolder viewHolder;
            if(convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.wallpaperImage = (ImageView) view.findViewById(R.id.wallpaper_image);
                viewHolder.wallpaperName = (TextView) view.findViewById(R.id.wallpaper_name);
                view.setTag(viewHolder);

            } else {
                view = convertView;     // 避免重复加载布局造成性能瓶颈
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.wallpaperImage.setImageResource(wallpaper.getImageId());
            viewHolder.wallpaperName.setText(wallpaper.getName());
            return view;
        }
    }

    class ViewHolder {
        ImageView wallpaperImage;
        TextView wallpaperName;
    }

    private void initWallpapers() {
        for(int i=0; i< wallpapersNames.length; i++) {
            Wallpaper wallpaper = new Wallpaper(wallpapersNames[i], wallpapersIds[i]);
            wallpaperList.add(wallpaper);
        }
    }

}