package com.nightmare.applib;

import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.nightmare.applib.handler.AppInfosHandler;
import com.nightmare.applib.handler.IconHandler;
import com.nightmare.applib.utils.BitmapHelper;
import com.nightmare.applib.utils.ReflectUtil;
import com.nightmare.applib.wrappers.DisplayManagerRef;
import com.nightmare.applib.wrappers.IPackageManager;
import com.nightmare.applib.wrappers.ServiceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nightmare.applib.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Nightmare on 2021/7/29.
 */


public class AppChannel {
    private static final String TAG = "app_channel";
    IPackageManager pm;
    DisplayManagerRef displayManagerRef;
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
        // 下面这个尽量别换成lambda,一个lambda编译后的产物会多一个class
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    DisplayManager dm = (DisplayManager) fakeContext.getSystemService(Context.DISPLAY_SERVICE);
                    dm.registerDisplayListener(new DisplayManager.DisplayListener() {
                        @Override
                        public void onDisplayAdded(int displayId) {
                            L.d("onDisplayAdded invoked displayId:" + displayId);
                        }

                        @Override
                        public void onDisplayRemoved(int displayId) {
                            L.d("onDisplayRemoved invoked displayId:" + displayId);

                        }

                        @Override
                        public void onDisplayChanged(int displayId) {
                            L.d("onDisplayChanged invoked displayId:" + displayId);
                            Display display = dm.getDisplay(displayId);
                            L.d("onDisplayChanged getRefreshRate:" + display.getRefreshRate());
                        }
                    }, null);
                    Looper.loop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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



    public byte[] getApkBitmapBytes(String path) throws
            InvocationTargetException, IllegalAccessException {
        return BitmapHelper.bitmap2Bytes(getUninstallAPKIcon(path));
    }


    AssetManager getAssetManagerFromPath(String path) {
        AssetManager assetManager = null;
        try {
            assetManager = AssetManager.class.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        try {
            assert assetManager != null;
            assetManager.getClass().getMethod("addAssetPath", String.class).invoke(assetManager, path);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return assetManager;
    }


    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo info = packageInfo.applicationInfo;
            info.sourceDir = apkPath;
            info.publicSourceDir = apkPath;
            try {
                return info.loadIcon(packageManager);
            } catch (Exception e) {

            }
        }
        return null;
    }

    //
    public Bitmap getUninstallAPKIcon(String apkPath) {
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        Drawable icon = getApkIcon(context, apkPath);
        try {
            if (icon == null) {
                return null;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && icon instanceof AdaptiveIconDrawable) {
                Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                icon.draw(canvas);
                return bitmap;
            } else {
                return ((BitmapDrawable) icon).getBitmap();
            }
        } catch (Exception e) {
            return null;
        }
    }


    private boolean validatePackageName(int uid, String packageName) {
        if (packageName != null) {
            String[] packageNames = context.getPackageManager().getPackagesForUid(uid);
            if (packageNames != null) {
                for (String n : packageNames) {
                    if (n.equals(packageName)) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

}
