package com.example.uapp.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.uapp.R;

import org.litepal.crud.LitePalSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


public class LostItem extends LitePalSupport implements Serializable {
    //物品信息
    private String name;
    private int imageId;
    private Date lostTime;
    private String pos;
    private String desc;
    //帖子信息
    private String postId;//帖子id
    private String posterId = "null";//上传者id
    private Date postTime;//上传时间
    private boolean state;//是否找回
    private String imagePath;//图片存放路径
    private String thumbnailPath;//缩略图路径
    private String contact;//联系方式

    public LostItem(String name,int imageId,Date lostTime,String pos){
        this.name = name;
        this.imageId = imageId;
        this.lostTime = lostTime;
        this.pos = pos;
    }

    public LostItem(String name, String imagePath,Date lostTime,String pos){
        this.name = name;
        this.lostTime = lostTime;
        this.pos = pos;
        this.imagePath = imagePath;
    }

    public LostItem(String name, String imagePath,Date lostTime,String pos,String posterId,
                    Date postTime, boolean state, String postId ,String desc, String contact){
        this.name = name;
        this.lostTime = lostTime;
        this.pos = pos;
        this.imagePath = imagePath;
        this.posterId = posterId;
        this.postTime = postTime;
        this.state = state;
        this.postId = postId;
        this.desc = desc;
        this.contact = contact;

        File file = new File(imagePath);
        String parentPath = file.getParent();
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            fileName = fileName.substring(0, dotIndex);
        }
        thumbnailPath = parentPath + File.separator + fileName + "_thumbnail.jpg";  // 构建新图片的路径

    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getPos(){
        return pos;
    }

    public void setPos(String pos){
        this.pos = pos;
    }

    public Date getLostTime(){
        return lostTime;
    }

    public void setLostTime(Date lostTime){
        this.lostTime = lostTime;
    }

    public int getImageId(){
        return imageId;
    }

    public void setImageId(int imageId){
        this.imageId = imageId;
    }

    public String getDesc(){
        return this.desc;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public String getPosterId(){
        return this.posterId;
    }

    public void setPosterId(String posterId){
        this.posterId = posterId;
    }

    public Date getPostTime(){
        return this.postTime;
    }

    public void setPostTime(Date postTime){
        this.postTime = postTime;
    }

    public boolean getState(){
        return this.state;
    }

    public void setState(boolean state){
        this.state = state;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath(){return this.imagePath;}

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostId(){return this.postId;}

    public String getThumbnailPath(){return this.thumbnailPath;}

    public String getContact(){return this.contact;}
    public void setContact(String contact){this.contact = contact;}
}


//class LostItemAdapter extends ArrayAdapter<LostItem> {
//    private int resourceId;
//
//    public LostItemAdapter(Context context, int textViewResourceId, List<LostItem> objects) {
//        super(context, textViewResourceId, objects);
//        resourceId = textViewResourceId;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        LostItem lostItem = getItem(position);
//        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
//        ImageView itemImage = (ImageView) view.findViewById(R.id.itemImage);
//        TextView itemName = (TextView) view.findViewById(R.id.itemName);
//        TextView lostTime = (TextView) view.findViewById(R.id.lostTime);
//        TextView pos = (TextView) view.findViewById(R.id.pos);
//        itemImage.setImageResource(lostItem.getImageId());
//        itemName.setText(lostItem.getName());
//        lostTime.setText(lostItem.getLostTime().toString());
//        pos.setText(lostItem.getPos());
//        return view;
//    }
//}
