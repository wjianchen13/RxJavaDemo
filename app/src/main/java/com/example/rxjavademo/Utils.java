package com.example.rxjavademo;

import android.content.Context;
import android.widget.Toast;

public class Utils {
    
    public static void log(String str) {
        System.out.println("========================> " + str);
    }

    public static void showToast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static String getThreadName() {
        return Thread.currentThread().getName();
    }

}
