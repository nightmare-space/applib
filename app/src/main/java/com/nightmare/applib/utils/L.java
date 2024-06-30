package com.nightmare.applib.utils;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class L {
    private static PrintStream fileOut;

    public static void d(Object object) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) 0x1b + "[38;5;42m·");
        sb.append((char) 0x1b + "[0m ");
        sb.append((char) 0x1b + "[38;5;38m");
        sb.append(object.toString());
        sb.append((char) 0x1b + "[0m");
        Log.d("applib",object.toString());
        System.out.println(sb);
        System.out.flush();

        // Append to file
        try {
            if (fileOut == null) {
                fileOut = new PrintStream(new FileOutputStream("/sdcard/app_server_log", false));
            }
            fileOut.println(sb);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void e(Object object) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) 0x1b + "[38;5;42m·");
        sb.append((char) 0x1b + "[0m ");
        sb.append((char) 0x1b + "[38;5;38m");
        sb.append(object.toString());
        sb.append((char) 0x1b + "[0m");
        Log.d("applib",object.toString());
        System.out.println(sb);
        System.out.flush();

        // Append to file
        try {
            if (fileOut == null) {
                fileOut = new PrintStream(new FileOutputStream("/sdcard/app_server_log", false));
            }
            fileOut.println(sb);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}