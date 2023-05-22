package com.example.uapp.utils;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.allen.library.SuperTextView;

public class AppearanceUtils {
    public static void increaseFontSize(View view, float scaleFactor) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            float currentSize = textView.getTextSize();
            float newSize = currentSize * scaleFactor;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        } else if (view instanceof EditText) {
            EditText editText = (EditText) view;
            float currentSize = editText.getTextSize();
            float newSize = currentSize * scaleFactor;
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        } else if (view instanceof SuperTextView) {
            SuperTextView superTextView = (SuperTextView) view;
            if(scaleFactor == 0f){
                superTextView.invalidate();
                return;
            }
            float currentSize = superTextView.getLeftTextView().getTextSize();
            float newSize = currentSize * scaleFactor;
            superTextView.getLeftTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX,newSize);
            int currentHeight = superTextView.getHeight();
            int newHeight = (int)(currentHeight * scaleFactor);
            ViewGroup.LayoutParams params = superTextView.getLayoutParams();
            params.height = newHeight;
            superTextView.setLayoutParams(params);
            Log.d("========debug_sv_scaleFactor========", " "+scaleFactor);
            Log.d("========debug_sv_old_size========", " "+currentSize);
            Log.d("========debug_sv_new_size========", " "+newSize);
            superTextView.invalidate();
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            float currentSize = imageView.getWidth();
            float newSize = currentSize * scaleFactor;
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = (int) newSize;
            params.height = (int) newSize;
            Log.d("========debug_iv_scaleFactor========", " "+scaleFactor);
            Log.d("========debug_iv_old_size========", " "+currentSize);
            Log.d("========debug_iv_new_size========", " "+newSize);
            imageView.setLayoutParams(params);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = viewGroup.getChildAt(i);
                increaseFontSize(childView, scaleFactor);
            }
        } else if (view instanceof ListView) {
            ListView listView = (ListView) view;
            ListAdapter adapter = listView.getAdapter();
            if (adapter != null) {
                int count = adapter.getCount();
                for (int i = 0; i < count; i++) {
                    View listItem = adapter.getView(i, null, listView);
                    increaseFontSize(listItem, scaleFactor);
                }
            }
        }
    }
}
