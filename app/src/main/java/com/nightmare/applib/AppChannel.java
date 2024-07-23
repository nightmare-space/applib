package com.nightmare.applib;

import static android.content.Context.WINDOW_SERVICE;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
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
import android.media.MediaCodec;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import android.media.MediaCodec;

import com.nightmare.applib.utils.ReflectUtil;
import com.nightmare.applib.utils.Workarounds;
import com.nightmare.applib.wrappers.DisplayManagerRef;
import com.nightmare.applib.wrappers.IPackageManager;
import com.nightmare.applib.wrappers.ServiceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    Context context;
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
                    L.d("fakeContext ->" + fakeContext.toString());
                    // 恢复输出
//                    System.setOut(console);
//                    System.setErr(console);
//                    L.d("icon get start");
                    DisplayManager dm = (DisplayManager) fakeContext.getSystemService(Context.DISPLAY_SERVICE);
//                    void setGlobalUserPreferredDisplayMode(android.view.Display$Mode arg0,)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
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
                    }
//                    Display display = wm.getDefaultDisplay();
//                    ReflectUtil.listAllObject(displayManager);
//                    ReflectUtil.listAllObject(displays[0]);
//                    for (Display display : displays) {
//                        Display.Mode[] modes = display.getSupportedModes();
////                        L.d("modes -> " + Arrays.toString(modes));
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                            L.d("start->"+display.getMode());
//                        }
//                        if (display.getDisplayId() != 0) {
//                            Method method = null;
//                            try {
//                                method = display.getClass().getDeclaredMethod("setUserPreferredDisplayMode", Display.Mode.class);
//                                method.setAccessible(true);
//                                method.invoke(display, modes[2]);
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                                    L.d("end->"+display.getMode());
//                                }
//                            } catch (IllegalAccessException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                    }
//                  display.setUserPreferredDisplayMode
                    L.d("获取到的Context:" + context.toString());
                    Looper.loop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void testFunc(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        FrameLayout unconViewWrapper = new FrameLayout(context);
        unconViewWrapper.setBackgroundColor(Color.BLUE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.width = 0;
        layoutParams.height = 0;
//        layoutParams.gravity = Gravity.TOP & Gravity.LEFT;
//        window.setGravity(Gravity.CENTER);
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN//将window放置在整个屏幕之内,无视其他的装饰(比如状态栏)
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS//允许window扩展值屏幕之外
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_FULLSCREEN//当这个window显示的时候,隐藏所有的装饰物(比如状态栏)这个flag允许window使用整个屏幕区域
                | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER//标记在其它窗口的LayoutParams.flags中的存在情况而不断地被调整
        ;

        windowManager.addView(unconViewWrapper, layoutParams);

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


    //    JSONObject jsonObject = new JSONObject();
//    DisplayMetrics metrics = new DisplayMetrics();
//        display.getMetrics(metrics);
//        jsonObject.put("id", display.getDisplayId());
//        jsonObject.put("metrics", metrics.toString());
//        jsonObject.put("name", display.getName());
//        jsonObject.put("width", display.getWidth());
//        jsonObject.put("height", display.getHeight());
//        jsonObject.put("rotation", display.getRotation());
//        jsonObject.put("refreshRate", display.getRefreshRate());
//        jsonObject.put("density", metrics.densityDpi);
//        jsonObject.put("dump", display.toString());
    public String getAppInfos(List<String> packages) {
        StringBuilder builder = new StringBuilder();
        for (String packageName : packages) {
            PackageInfo packageInfo = null;
            try {
                packageInfo = getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (packageInfo != null) {
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                builder.append(applicationInfo.packageName);
                builder.append("\r").append(getLabel(applicationInfo));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.append("\r").append(packageInfo.applicationInfo.minSdkVersion);
                } else {
                    builder.append("\r").append("null");
                }
                builder.append("\r").append(applicationInfo.targetSdkVersion);
                builder.append("\r").append(packageInfo.versionName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    builder.append("\r").append(packageInfo.getLongVersionCode());
                } else {
                    builder.append("\r").append(packageInfo.versionCode);
                }
                builder.append("\r").append(applicationInfo.enabled);
                try {
                    // 只有被隐藏的app会拿不到
                    PackageInfo withoutHidePackage = getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
//                    Log.w("Nightmare", withoutHidePackage.applicationInfo.loadLabel(context.getPackageManager()) + "");
                    builder.append("\r").append(false);
                } catch (InvocationTargetException e) {
                    L.d(packageInfo.packageName + "为隐藏app");
                    builder.append("\r").append(true);
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                builder.append("\r").append(applicationInfo.uid);
                builder.append("\r").append(applicationInfo.sourceDir);
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    //    public void getAllAppInfo(boolean isSystemApp, boolean print) {
//        if (print) {
//            System.out.print(getAllAppInfo(isSystemApp));
//        }
//    }
    public List<String> getAppPackages() {
        if (context != null) {
            PackageManager pm = context.getPackageManager();
//            context.getSystemService(Context.ACTIVITY_SERVICE);
            List<String> packages = new ArrayList<String>();
            List<PackageInfo> infos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            for (PackageInfo info : infos) {
                packages.add(info.packageName);
            }
            return packages;
        } else {
            return pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        }
    }

    public PackageInfo getPackageInfo(String packageName) throws InvocationTargetException, IllegalAccessException {
        return getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
    }

    /**
     * @noinspection CallToPrintStackTrace
     */
    public PackageInfo getPackageInfo(String packageName, int flag) throws InvocationTargetException, IllegalAccessException {
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = null;
            try {
                info = pm.getPackageInfo(packageName, flag);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return info;
        } else {
            return pm.getPackageInfo(packageName, flag);
        }
    }

    public String getAllAppInfoV2(boolean isSystemApp) throws InvocationTargetException, IllegalAccessException, JSONException {
        List<String> packages = getAppPackages();
        if (packages == null) {
            return "";
        }
        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (String packageName : packages) {
            JSONObject appInfoJson = new JSONObject();
            PackageInfo packageInfo = getPackageInfo(packageName);
            if (packageInfo == null) {
                continue;
            }
            int resultTag = packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM;
            if (isSystemApp) {
                // 是否只列表系统应用,=0为用户应用，跳过
                if (resultTag == 0) {
                    continue;
                }
            } else {
                if (resultTag > 0) {
                    continue;
                }
            }
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            appInfoJson.put("package", applicationInfo.packageName);
            appInfoJson.put("label", getLabel(applicationInfo));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                appInfoJson.put("minSdk", packageInfo.applicationInfo.minSdkVersion);
            } else {
                appInfoJson.put("minSdk", 0);
            }
            appInfoJson.put("targetSdk", applicationInfo.targetSdkVersion);
            appInfoJson.put("versionName", packageInfo.versionName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appInfoJson.put("versionCode", packageInfo.getLongVersionCode());
            } else {
                appInfoJson.put("versionCode", packageInfo.versionCode);
            }
            appInfoJson.put("enabled", applicationInfo.enabled);
            try {
                PackageInfo withoutHidePackage = getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
                appInfoJson.put("hide", false);
            } catch (InvocationTargetException | IllegalAccessException e) {
                appInfoJson.put("hide", true);
            }
            appInfoJson.put("uid", applicationInfo.uid);
            appInfoJson.put("sourceDir", applicationInfo.sourceDir);
            jsonArray.put(appInfoJson);
        }
        jsonObjectResult.put("datas", jsonArray);
        return jsonObjectResult.toString();
    }

    // 这儿有crash
    public String getAllAppInfo(boolean isSystemApp) throws InvocationTargetException, IllegalAccessException {
        List<String> packages = getAppPackages();
        if (packages == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String packageName : packages) {
            PackageInfo packageInfo = getPackageInfo(packageName);
//            ActivityInfo[] activityInfos = packageInfo.activities;
//            for (ActivityInfo info : activityInfos) {
//                print(packageName + " Activity Info" + info.name);
//            }
            if (packageInfo == null) {
                continue;
            }
            int resultTag = packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM;
            if (isSystemApp) {
                // 是否只列表系统应用
                if (resultTag <= 0) {
                    // <=0为用户应用，跳过
                    continue;
                }
            } else {
                if (resultTag > 0) {
                    continue;
                }
            }
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            builder.append(applicationInfo.packageName);
//            Log.d("Nightmare", "getAllAppInfo package:" + applicationInfo.packageName);
            builder.append("\r").append(getLabel(applicationInfo));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.append("\r").append(packageInfo.applicationInfo.minSdkVersion);
            } else {
                builder.append("\r").append("null");
            }
            builder.append("\r").append(applicationInfo.targetSdkVersion);
            builder.append("\r").append(packageInfo.versionName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.append("\r").append(packageInfo.getLongVersionCode());
            } else {
                builder.append("\r").append(packageInfo.versionCode);
            }
            builder.append("\r").append(applicationInfo.enabled);
            try {
                // 只有被隐藏的app会拿不到，所以捕捉到异常后，标记这个app就是被隐藏的
                // TODO 下面这行代码在Android下有异常
                PackageInfo withoutHidePackage = getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
//                    Log.w("Nightmare", withoutHidePackage.applicationInfo.loadLabel(context.getPackageManager()) + "");
                builder.append("\r").append(false);
            } catch (InvocationTargetException | IllegalAccessException e) {
                builder.append("\r").append(true);
            }
            builder.append("\r").append(applicationInfo.uid);
            builder.append("\r").append(applicationInfo.sourceDir);
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    public String getLabel(ApplicationInfo info) {
        if (context != null) {
            // 如果已经有上下文
            PackageManager pm = context.getPackageManager();
            return (String) info.loadLabel(pm);
        }
        int res = info.labelRes;
        if (info.nonLocalizedLabel != null) {
            return (String) info.nonLocalizedLabel;
        }
        if (res != 0) {
            AssetManager assetManager = getAssetManagerFromPath(info.sourceDir);
            Resources resources = new Resources(assetManager, displayMetrics, configuration);
            return (String) resources.getText(res);
        }
        return null;
    }

    public void openApp(String packageName, String activity, String displayId) {
        if (!hasRealContext) {
            String cmd = "am start --display " + displayId + " -n " + packageName + "/" + activity;
            L.d("start activity cmd : " + cmd);
            // adb -s $serial shell am start -n $packageName/$activity
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // 取消activity动画
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK);
            ActivityOptions options = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                options = ActivityOptions.makeBasic().setLaunchDisplayId(Integer.parseInt(displayId));

                ReflectUtil.listAllObject(options);
//                options.setLaunchWindowingMode();

            }
            ComponentName cName = new ComponentName(packageName, activity);
            intent.setComponent(cName);
//            context.startActivity(intent);
            @SuppressLint({"NewApi", "LocalSuppress"})
            Bundle bundle = options.toBundle();
            bundle.putInt("android.activity.activityType", 2);
            context.startActivity(intent, options.toBundle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAppActivitys(String data) {
        StringBuilder builder = new StringBuilder();
        @SuppressLint("QueryPermissionsNeeded")
        List<String> packages = getAppPackages();
        for (String packageName : packages) {
            PackageInfo info = null;
            try {
                info = getPackageInfo(packageName);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (info.packageName.equals(data)) {
                try {
                    PackageInfo packageInfo = getPackageInfo(data);
                    if (packageInfo.activities == null) {
                        return "";
                    }
                    for (ActivityInfo activityInfo : packageInfo.activities) {
                        builder.append(activityInfo.name).append("\n");
                    }

                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    return builder.toString();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return builder.toString();
                }
            }

        }
        return builder.toString();

    }

    public String getAppPermissions(String data) {
        StringBuilder builder = new StringBuilder();
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo packageInfo = getPackageInfo(data, PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
                String[] usesPermissionsArray = packageInfo.requestedPermissions;
                for (String usesPermissionName : usesPermissionsArray) {

                    //得到每个权限的名字,如:android.permission.INTERNET
//                    print("usesPermissionName=" + usesPermissionName);
                    builder.append(usesPermissionName);
                    //通过usesPermissionName获取该权限的详细信息
                    PermissionInfo permissionInfo = pm.getPermissionInfo(usesPermissionName, 0);

                    //获得该权限属于哪个权限组,如:网络通信
//                PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
//                System.out.println("permissionGroup=" + permissionGroupInfo.loadLabel(packageManager).toString());

                    //获取该权限的标签信息,比如:完全的网络访问权限
                    String permissionLabel = getLabel(packageInfo.applicationInfo);
//                    print("permissionLabel=" + permissionLabel);

                    //获取该权限的详细描述信息,比如:允许该应用创建网络套接字和使用自定义网络协议
                    //浏览器和其他某些应用提供了向互联网发送数据的途径,因此应用无需该权限即可向互联网发送数据.
                    String permissionDescription = permissionInfo.loadDescription(pm).toString();

                    builder.append(" ").append(permissionDescription);
                    boolean isHasPermission = PackageManager.PERMISSION_GRANTED == pm.checkPermission(permissionInfo.name, data);
                    builder.append(" ").append(isHasPermission).append("\r");
//                    print("permissionDescription=" + permissionDescription);
//                    print("===========================================");
                }

            } catch (Exception e) {
                // TODO: handle exception
            }
            return builder.toString();
        }

        return builder.toString();

    }


    /*
     * 通过包名获取Main Activity
     * */
    public String getAppMainActivity(String packageName) {
        StringBuilder builder = new StringBuilder();
//        try {
//            Class<?> cls = Class.forName("android.app.ApplicationPackageManager");
//            ReflectUtil.listAllObject(cls);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

//        ReflectUtil.listAllObject(serviceManager.getPackageManager().manager.getClass());
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                builder.append(launchIntent.getComponent().getClassName());
            } else {
                L.d(packageName + "获取启动Activity失败");
            }
            return builder.toString();
        }
        // 注释掉的是另外一种方法
//        ReflectUtil.listAllObject(serviceManager.getPackageManager().manager.getClass());
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, null, 0, 0);
        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo resolveInfo = appList.get(i);
            String packageStr = resolveInfo.activityInfo.packageName;
            if (packageStr.equals(packageName)) {
                builder.append(resolveInfo.activityInfo.name).append("\n");
                break;
            }
        }
        return builder.toString();
    }

    public String getAppDetail(String data) {
        StringBuilder builder = new StringBuilder();
        try {
            PackageInfo packageInfo = getPackageInfo(data);
            builder.append(packageInfo.firstInstallTime).append("\r");
            builder.append(packageInfo.lastUpdateTime).append("\r");
            builder.append(packageInfo.applicationInfo.dataDir).append("\r");
            builder.append(packageInfo.applicationInfo.nativeLibraryDir);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }


    private static List<String> stringToList(String strs) {
        String[] str = strs.split(" ");
        return Arrays.asList(str);
    }

    public byte[] getBitmapBytes(String packname) throws
            InvocationTargetException, IllegalAccessException {
        return Bitmap2Bytes(getBitmap(packname));
    }

    public byte[] getApkBitmapBytes(String path) throws
            InvocationTargetException, IllegalAccessException {
        return Bitmap2Bytes(getUninstallAPKIcon(path));
    }


    static public byte[] Bitmap2Bytes(Bitmap bm) {
        if (bm == null) {
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
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
//        try {
//            // apk包的文件路径
//            // 这是一个Package 解释器, 是隐藏的
//            // 构造函数的参数只有一个, apk文件的路径
//            // PackageParser packageParser = new PackageParser(apkPath);
//            Class pkgParserCls = Class.forName(PATH_PackageParser);
//            ReflectUtil.listAllObject(pkgParserCls);
//            Class[] typeArgs = new Class[1];
//            typeArgs[0] = String.class;
//            Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
//            Object[] valueArgs = new Object[1];
//            valueArgs[0] = apkPath;
//            Object pkgParser = pkgParserCt.newInstance(valueArgs);
//            Log.d("ANDROID_LAB", "pkgParser:" + pkgParser.toString());
//            // 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
//            DisplayMetrics metrics = new DisplayMetrics();
//            metrics.setToDefaults();
//            // PackageParser.Package mPkgInfo = packageParser.parsePackage(new
//            // File(apkPath), apkPath,
//            // metrics, 0);
//            typeArgs = new Class[4];
//            typeArgs[0] = File.class;
//            typeArgs[1] = String.class;
//            typeArgs[2] = DisplayMetrics.class;
//            typeArgs[3] = Integer.TYPE;
//            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
//            valueArgs = new Object[4];
//            valueArgs[0] = new File(apkPath);
//            valueArgs[1] = apkPath;
//            valueArgs[2] = metrics;
//            valueArgs[3] = 0;
//            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
//            // 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
//            // ApplicationInfo info = mPkgInfo.applicationInfo;
//            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
//            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
//            // uid 输出为"-1"，原因是未安装，系统未分配其Uid。
//            Log.d("ANDROID_LAB", "pkg:" + info.packageName + " uid=" + info.uid);
//            AssetManager assetManager = getAssetManagerFromPath(info.sourceDir);
//            Resources resources = new Resources(assetManager, displayMetrics, configuration);
//            CharSequence label = null;
//            if (info.labelRes != 0) {
//                label = resources.getText(info.labelRes);
//            }
//            Log.d("ANDROID_LAB", "label=" + label);
//            // 这里就是读取一个apk程序的图标
//            if (info.icon != 0) {
//                icon = resources.getDrawable(info.icon);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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


    /**
     * @param packageName
     * @return 应用的Bitmap对象
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public synchronized Bitmap getBitmap(String packageName) throws
            InvocationTargetException, IllegalAccessException {
        Drawable icon = null;
//        if (null != context) {
//            PackageManager pm = context.getPackageManager();
//            try {
////                PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
////                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
//
//                icon = pm.getApplicationIcon(packageName);
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
////            context.getDrawable(pm)
//        } else {
//        ReflectUtil.listAllObject(pm);
        PackageInfo packageInfo = getPackageInfo(packageName);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if (applicationInfo == null) {
            L.d("applicationInfo == null");
            return null;
        }
//        Log.d("Nightmare", "getBitmap package:" + applicationInfo.packageName + "icon:" + applicationInfo.icon);
        // L.d("getBitmap package:" + applicationInfo.packageName + " icon:" + applicationInfo.icon);
        // L.d("applicationInfo.sourceDir:" + applicationInfo.sourceDir);
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
            assetManager.getClass().getMethod("addAssetPath", String.class).invoke(assetManager, applicationInfo.sourceDir);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Resources resources = new Resources(assetManager, displayMetrics, configuration);
        try {
//            icon = applicationInfo.loadIcon(pm);
            icon = resources.getDrawable(applicationInfo.icon, null);
        } catch (Exception e) {
            L.d("getBitmap package error:" + applicationInfo.packageName);
//            e.printStackTrace();
            return null;
        }
//        }

        try {
            if (icon == null) {
//                print(applicationInfo.packageName + "icon null");
                return null;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && icon instanceof AdaptiveIconDrawable) {
                Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                icon.draw(canvas);
                return bitmap;
            } else {
                int w = icon.getIntrinsicWidth();
                int h = icon.getIntrinsicHeight();
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                //设置画布的范围
                icon.setBounds(0, 0, w, h);
                icon.draw(canvas);
                return bitmap;
//                return ((BitmapDrawable) icon).getBitmap();
            }
        } catch (Exception e) {
            L.d("Exception:" + e);
            return null;
        }
    }

    //
//    public static Drawable getDrawable(Context context, int id) {
//        if (Build.VERSION.SDK_INT >= 21) {
//            return context.getDrawable(id);
//        } else if (Build.VERSION.SDK_INT >= 16) {
//            return context.getResources().getDrawable(id);
//        }
//        return null;
//    }
    int textureID = 10;

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
