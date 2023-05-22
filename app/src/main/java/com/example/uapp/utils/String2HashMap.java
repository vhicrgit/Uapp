package com.example.uapp.utils;

import java.util.HashMap;

public class String2HashMap {
    // 将 HashMap 转换为字符串
    public static String hashMapToString(HashMap<String, String> hashMap) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : hashMap.keySet()) {
            String value = hashMap.get(key);
            stringBuilder.append(key).append(":").append(value).append(",");
        }

        // 去除最后一个逗号
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }

        return stringBuilder.toString();
    }

    // 将字符串解析为 HashMap
    public static HashMap<String, String> stringToHashMap(String str) {
        HashMap<String, String> hashMap = new HashMap<>();

        String[] pairs = str.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                hashMap.put(key, value);
            }
        }

        return hashMap;
    }
}
