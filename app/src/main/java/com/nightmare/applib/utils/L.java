package com.nightmare.applib.utils;

import android.util.Log;

public class L {
    public static void d(Object object) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) 0x1b + "[38;5;42m·");
        sb.append((char) 0x1b + "[0m ");
        sb.append((char) 0x1b + "[38;5;38m");
        sb.append(object.toString());
        Log.d("applib",sb.toString());
        sb.append((char) 0x1b + "[0m");
        Log.d("applib",object.toString());
        System.out.println(sb);
        System.out.flush();
    }
}
