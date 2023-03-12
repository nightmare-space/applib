package com.nightmare.applib.utils;

import android.util.Log;

public class L {
    public static void d(Object object) {
        System.out.print((char) 0x1b + "[38;5;42m·");
        System.out.print((char) 0x1b + "[0m ");
        System.out.print((char) 0x1b + "[38;5;38m");
        System.out.print(object.toString());
        Log.d("applib",object.toString());
        System.out.println((char) 0x1b + "[0m");
        System.out.flush();
    }
}
