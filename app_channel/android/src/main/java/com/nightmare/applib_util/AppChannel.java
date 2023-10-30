package com.nightmare.applib_util;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Application;
import android.app.Instrumentation;
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
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;

import com.nightmare.applib.wrappers.IPackageManager;
import com.nightmare.applib.wrappers.ServiceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nightmare.applib_util.utils.Lg;


/**
 * Created by Nightmare on 2021/7/29.
 */

public class AppChannel {
    private static final String TAG = "app_channel";
    IPackageManager pm;
    com.nightmare.applib.wrappers.DisplayManager displayManager;
    ServiceManager serviceManager;
    static final String SOCKET_NAME = "app_manager";
    static final int RANGE_START = 6000;
    static final int RANGE_END = 6040;
    DisplayMetrics displayMetrics;
    Configuration configuration;
    Context context;

    // 没有context的时候的构造函数，用于dex中创建这个对象
    public AppChannel() {
        displayMetrics = new DisplayMetrics();
        displayMetrics.setToDefaults();
        configuration = new Configuration();
        configuration.setToDefaults();
        serviceManager = new ServiceManager();
        pm = serviceManager.getPackageManager();
        // 下面这个尽量别换成lambda,一个lambda编译后的产物会多一个class
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Looper.prepare() Looper.loop() 不能移除
                    Looper.prepare();
                    FileOutputStream fileOutputStream = new FileOutputStream(new File("/data/local/tmp/dex_cache"), false);
                    PrintStream console = System.out;
                    // 重定向输出，因为fillAppInfo会有一堆报错
                    System.setErr(new PrintStream(fileOutputStream, false));
                    System.setOut(new PrintStream(fileOutputStream, false));
                    context = Workarounds.fillAppInfo();
                    // 恢复输出
                    System.setOut(console);
                    System.setErr(console);
                    Looper.loop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public AppChannel(Context context) {
        displayMetrics = new DisplayMetrics();
        displayMetrics.setToDefaults();
        configuration = new Configuration();
        configuration.setToDefaults();
        serviceManager = new ServiceManager();
        pm = serviceManager.getPackageManager();
        this.context = context;
    }


    public static Context getContextWithoutActivity() throws Exception {
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Constructor<?> activityThreadConstructor = activityThreadClass.getDeclaredConstructor();
        activityThreadConstructor.setAccessible(true);
        Object activityThread = activityThreadConstructor.newInstance();

        // ActivityThread.sCurrentActivityThread = activityThread;
        Field sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
        sCurrentActivityThreadField.setAccessible(true);
        sCurrentActivityThreadField.set(null, activityThread);

        // ActivityThread.AppBindData appBindData = new ActivityThread.AppBindData();
        Class<?> appBindDataClass = Class.forName("android.app.ActivityThread$AppBindData");
        Constructor<?> appBindDataConstructor = appBindDataClass.getDeclaredConstructor();
        appBindDataConstructor.setAccessible(true);
        Object appBindData = appBindDataConstructor.newInstance();

        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.packageName = "com.android.shell";

        // appBindData.appInfo = applicationInfo;
        Field appInfoField = appBindDataClass.getDeclaredField("appInfo");
        appInfoField.setAccessible(true);
        appInfoField.set(appBindData, applicationInfo);

        // activityThread.mBoundApplication = appBindData;
        Field mBoundApplicationField = activityThreadClass.getDeclaredField("mBoundApplication");
        mBoundApplicationField.setAccessible(true);
        mBoundApplicationField.set(activityThread, appBindData);

        // Context ctx = activityThread.getSystemContext();
        Method getSystemContextMethod = activityThreadClass.getDeclaredMethod("getSystemContext");

        Context ctx = (Context) getSystemContextMethod.invoke(activityThread);
        Application app = Instrumentation.newApplication(Application.class, ctx);

        // activityThread.mInitialApplication = app;
        Field mInitialApplicationField = activityThreadClass.getDeclaredField("mInitialApplication");
        mInitialApplicationField.setAccessible(true);
        mInitialApplicationField.set(activityThread, app);
        return ctx;
    }

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
                    packageInfo = getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
//                    Log.w("Nightmare", withoutHidePackage.applicationInfo.loadLabel(context.getPackageManager()) + "");
                    builder.append("\r").append(false);
                } catch (InvocationTargetException e) {
                    Lg.d(packageInfo.packageName + "为隐藏app");
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
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = null;
            try {
                info = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return info;
        } else {
            return pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
        }
    }

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
//            Log.e("Nightmare", packageInfo.versionName + "");
            // 修复
            //  3.2rc
            //        破解软件TG频道：@fun_apk
            builder.append("\r").append(packageInfo.versionName.replaceAll("\n", ""));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.append("\r").append(packageInfo.getLongVersionCode());
            } else {
                builder.append("\r").append(packageInfo.versionCode);
            }
            builder.append("\r").append(applicationInfo.enabled);
            try {
                // 只有被隐藏的app会拿不到，所以捕捉到异常后，标记这个app就是被隐藏的
                PackageInfo withoutHidePackage = getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
//                    Log.w("Nightmare", withoutHidePackage.applicationInfo.loadLabel(context.getPackageManager()) + "");
                builder.append("\r").append(false);
            } catch (InvocationTargetException | IllegalAccessException e) {
                builder.append("\r").append(true);
            }
//            Log.e("Nightmare", applicationInfo.uid + "");
            builder.append("\r").append(applicationInfo.uid);
//            Log.e("Nightmare", applicationInfo.sourceDir);
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
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//           intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            ActivityOptions options = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                options = ActivityOptions.makeBasic();
                options.setLaunchDisplayId(Integer.parseInt(displayId));
            }
            ComponentName cName = new ComponentName(packageName, activity);
            intent.setComponent(cName);
            // context.startActivity(intent);
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
                Lg.d(packageName + "获取启动Activity失败");
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

    public byte[] getBitmapBytes(String packname) throws InvocationTargetException, IllegalAccessException {
        return Bitmap2Bytes(getBitmap(packname));
    }

    public byte[] getApkBitmapBytes(String path) throws InvocationTargetException, IllegalAccessException {
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

    public synchronized Bitmap getBitmap(String packageName) throws InvocationTargetException, IllegalAccessException {
        Drawable icon = null;
        if (null != context) {
            PackageManager pm = context.getPackageManager();
            try {
//                PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
//                ApplicationInfo applicationInfo = packageInfo.applicationInfo;

                icon = pm.getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
//            context.getDrawable(pm)
        } else {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if (applicationInfo == null) {
                Lg.d("applicationInfo == null");
                return null;
            }
//        Log.d("Nightmare", "getBitmap package:" + applicationInfo.packageName + "icon:" + applicationInfo.icon);
            Lg.d("getBitmap package:" + applicationInfo.packageName + " icon:" + applicationInfo.icon);
            Lg.d("applicationInfo.sourceDir:" + applicationInfo.sourceDir);
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
                Lg.d("getBitmap package error:" + applicationInfo.packageName);
                Lg.d("getBitmap package error:" + applicationInfo.packageName);
//            e.printStackTrace();
                return null;
            }
        }

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
            Lg.d("Exception:" + e);
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
