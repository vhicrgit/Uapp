package com.example.uapp.bean;

public class ImagePart {
    private String name; // 分段名称
    private String data; // 分段数据
    private int seq; // 分段序号
    private int length; // 分段长度

    public ImagePart(String name, String data, int seq, int length) {
        this.name = name;
        this.data = data;
        this.seq = seq;
        this.length = length;
    }
}
