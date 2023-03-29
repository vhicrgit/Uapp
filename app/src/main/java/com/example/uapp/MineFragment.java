package com.example.uapp;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;



public class MineFragment extends Fragment {

    private Button buttonTakePhoto;
    private Button buttonGetLocation;
    private Button buttonUploadPhoto;
    private ImageView imageShow;
    private TextView textLocation;

    private static final int GET_THUMBNAIL_CODE = 1;
    private static final int GET_GPS_CODE = 2;
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1;
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 2;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        buttonGetLocation = view.findViewById(R.id.get_location);
        buttonTakePhoto = view.findViewById(R.id.take_photo);
        buttonUploadPhoto = view.findViewById(R.id.upload_photo);
        imageShow = view.findViewById(R.id.show_photo);
        textLocation = view.findViewById(R.id.show_location);

        initLocation();

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ContextCompat.checkSelfPermission( getActivity(),android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                        new String[]{ android.Manifest.permission.CAMERA },
                        REQUEST_CAMERA_PERMISSION_CODE);
                } else {
                    Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(photoIntent, GET_THUMBNAIL_CODE);
                }
            }
        });

        return view;
    }

    private void initLocation() {

        if(ContextCompat.checkSelfPermission( getActivity(),android.Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( getActivity(),android.Manifest.permission.ACCESS_NETWORK_STATE)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( getActivity(),android.Manifest.permission.ACCESS_WIFI_STATE)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( getActivity(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( getActivity(),android.Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED


        ) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ android.Manifest.permission.INTERNET,
                            android.Manifest.permission.ACCESS_NETWORK_STATE,
                            android.Manifest.permission.ACCESS_WIFI_STATE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_PHONE_STATE
                    },
                    REQUEST_LOCATION_PERMISSION_CODE);
            return ;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case GET_THUMBNAIL_CODE:
                    Bundle extras = data.getExtras();
                    Bitmap bitmap = (Bitmap) extras.get("data");
                    imageShow.setImageBitmap(bitmap);
                    break;
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case REQUEST_CAMERA_PERMISSION_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //权限获取成功
                    Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(photoIntent, GET_THUMBNAIL_CODE);
                }else{
                    //权限被拒绝
                    Toast.makeText(getActivity(), "用户拒绝授予权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
