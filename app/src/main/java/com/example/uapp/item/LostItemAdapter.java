package com.example.uapp.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.uapp.R;

import com.example.uapp.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class LostItemAdapter extends ArrayAdapter<LostItem> {
    private int resourceId;

    public LostItemAdapter(Context context, int textViewResourceId, List<LostItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LostItem lostItem = getItem(position);
        View view;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        }
        else{
            view = convertView;
        }

//        ImageView itemImage = null;
//        if (lostItem.getImages() != null && lostItem.getImages().length > 0) {
//            Bitmap bitmap = BitmapFactory.decodeByteArray(lostItem.getImages(), 0, lostItem.getImages().length);
//            // 将Bitmap对象设置到ImageView中
//            itemImage.setImageBitmap(bitmap);
//        }else {
//            itemImage = (ImageView) view.findViewById(R.id.itemImage);
//        }
//        ImageView itemImage = null;
//        Bitmap bitmap = BitmapFactory.decodeFile(lostItem.getImagePath());
//        itemImage.setImageBitmap(bitmap);

        ImageView itemImage = (ImageView) view.findViewById(R.id.itemImage);
        TextView itemName = (TextView) view.findViewById(R.id.itemName);
        TextView lostTime = (TextView) view.findViewById(R.id.lostTime);
        TextView pos = (TextView) view.findViewById(R.id.pos);
//        itemImage.setImageResource(lostItem.getImageId());
        Bitmap bitmap = BitmapFactory.decodeFile(lostItem.getThumbnailPath());
        itemImage.setImageBitmap(bitmap);
        itemName.setText(lostItem.getName());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy年MM月dd日");
        String formattedDate1 = dateFormat1.format(lostItem.getLostTime());
        lostTime.setText(formattedDate1);
        pos.setText(lostItem.getPos());
        return view;
    }
}