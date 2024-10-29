package com.nightmare.applib;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.DisplayMetrics;

import com.nightmare.applib.wrappers.IPackageManager;
import com.nightmare.applib.wrappers.ServiceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.nightmare.applib.utils.L;


/**
 * Created by Nightmare on 2021/7/29.
 */


public class AppChannel {
    private static final String TAG = "app_channel";
    IPackageManager pm;
    static final String SOCKET_NAME = "app_manager";
    static final int RANGE_START = 6000;
    static final int RANGE_END = 6040;
    DisplayMetrics displayMetrics;
    Configuration configuration;
    public Context context;
    boolean hasRealContext = false;

    // 没有context的时候的构造函数，用于dex中创建这个对象
    public AppChannel() {
        L.d("AppChannel 无参构造");
        displayMetrics = new DisplayMetrics();
        displayMetrics.setToDefaults();
        configuration = new Configuration();
        configuration.setToDefaults();
        pm = ServiceManager.getPackageManager();
//        try {
//            ReflectUtil.listAllObject(Class.forName("android.view.SurfaceControl"));
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        new Thread(() -> {
            try {
                L.d("Runnable run");
                // Looper.prepare() Looper.loop() 不能移除
                Looper.prepare();
                FileOutputStream fileOutputStream = new FileOutputStream(new File("/data/local/tmp/dex_cache"), false);
                PrintStream console = System.out;
                // 重定向输出，因为fillAppInfo会有一堆报错
//                    System.setErr(new PrintStream(fileOutputStream, false));
//                    System.setOut(new PrintStream(fileOutputStream, false));
                FakeContext fakeContext = FakeContext.get();
                context = fakeContext;
                L.d("Context -> " + fakeContext.toString());
                // 恢复输出
//                    System.setOut(console);
//                    System.setErr(console);
//                    L.d("icon get start");
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public AppChannel(Context context) {
        displayMetrics = new DisplayMetrics();
        displayMetrics.setToDefaults();
        configuration = new Configuration();
        configuration.setToDefaults();
        pm = ServiceManager.getPackageManager();
        this.context = context;
        hasRealContext = true;
    }

//    public String getAppInfos(List<String> packages) {
//        StringBuilder builder = new StringBuilder();
//        for (String packageName : packages) {
//            PackageInfo packageInfo = null;
//            try {
//                packageInfo = getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//            if (packageInfo != null) {
//                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
//                builder.append(applicationInfo.packageName);
//                builder.append("\r").append(getLabel(applicationInfo));
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    builder.append("\r").append(packageInfo.applicationInfo.minSdkVersion);
//                } else {
//                    builder.append("\r").append("null");
//                }
//                builder.append("\r").append(applicationInfo.targetSdkVersion);
//                builder.append("\r").append(packageInfo.versionName);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    builder.append("\r").append(packageInfo.getLongVersionCode());
//                } else {
//                    builder.append("\r").append(packageInfo.versionCode);
//                }
//                builder.append("\r").append(applicationInfo.enabled);
//                try {
//                    // 只有被隐藏的app会拿不到
//                    PackageInfo withoutHidePackage = getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
////                    Log.w("Nightmare", withoutHidePackage.applicationInfo.loadLabel(context.getPackageManager()) + "");
//                    builder.append("\r").append(false);
//                } catch (InvocationTargetException e) {
//                    L.d(packageInfo.packageName + "为隐藏app");
//                    builder.append("\r").append(true);
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//                builder.append("\r").append(applicationInfo.uid);
//                builder.append("\r").append(applicationInfo.sourceDir);
//            }
//            builder.append("\n");
//        }
//        return builder.toString().trim();
//    }

    //    public void getAllAppInfo(boolean isSystemApp, boolean print) {
//        if (print) {
//            System.out.print(getAllAppInfo(isSystemApp));
//        }
//    }








}
