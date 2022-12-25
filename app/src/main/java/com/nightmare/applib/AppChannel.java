package com.nightmare.applib;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;


import com.nightmare.applib.wrappers.IPackageManager;
import com.nightmare.applib.wrappers.ServiceManager;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public AppChannel() {
        displayMetrics = new DisplayMetrics();
        displayMetrics.setToDefaults();
        configuration = new Configuration();
        configuration.setToDefaults();
        serviceManager = new ServiceManager();
        pm = serviceManager.getPackageManager();
//        displayManager = serviceManager.getDisplayManager();
//        print("......" + Arrays.toString(displayManager.getDisplayIds()));
//        SurfaceTexture texture = new SurfaceTexture(textureID);
        textureID++;
        print("准备创建1");
//        Surface surface = new Surface(texture);
//        int id = displayManager.createVirtualDisplay("com.android.shell", "uncon-vd", 100, 100, 300, surface, DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
//                DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION |
//                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC |
//                1 << 7);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    Workarounds.fillAppInfo();
                    context = getContextWithoutActivity();
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

    public static void print(Object object) {
        System.out.println(">>>>" + object.toString());
        System.out.flush();
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
                    PackageInfo withoutHidePackage = pm.getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
//                    Log.w("Nightmare", withoutHidePackage.applicationInfo.loadLabel(context.getPackageManager()) + "");
                    builder.append("\r").append(false);
                } catch (InvocationTargetException e) {
                    print(packageInfo.packageName + "为隐藏app");
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
            context.getSystemService(Context.ACTIVITY_SERVICE);
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
            builder.append("\r").append(packageInfo.versionName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.append("\r").append(packageInfo.getLongVersionCode());
            } else {
                builder.append("\r").append(packageInfo.versionCode);
            }
            builder.append("\r").append(applicationInfo.enabled);
            try {
                // 只有被隐藏的app会拿不到，所以捕捉到异常后，标记这个app就是被隐藏的
                PackageInfo withoutHidePackage = pm.getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
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
            PackageManager pm = context.getPackageManager();
            return (String) info.loadLabel(pm);
        }
        int res = info.labelRes;
        if (info.nonLocalizedLabel != null) {
            return (String) info.nonLocalizedLabel;
        }
        if (res != 0) {
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
                assetManager.getClass().getMethod("addAssetPath", String.class).invoke(assetManager, info.sourceDir);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            print("getLabel:" + info.packageName);
            Resources resources = new Resources(assetManager, displayMetrics, configuration);
            return (String) resources.getText(res);
        }
        return null;
    }

    public void openApp(String packageName, String activity) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cName = new ComponentName(packageName, activity);
            intent.setComponent(cName);
            context.startActivity(intent);
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
                print(packageName + "获取启动Activity失败");
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


    static public byte[] Bitmap2Bytes(Bitmap bm) {
        if (bm == null) {
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public synchronized Bitmap getBitmap(String packageName) throws InvocationTargetException, IllegalAccessException {
        PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if (applicationInfo == null) {
            print("applicationInfo == null");
            return null;
        }
//        Log.d("Nightmare", "getBitmap package:" + applicationInfo.packageName + "icon:" + applicationInfo.icon);
        print("getBitmap package:" + applicationInfo.packageName + " icon:" + applicationInfo.icon);
        print("applicationInfo.sourceDir:" + applicationInfo.sourceDir);
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
        Drawable icon;
        try {
//            icon = applicationInfo.loadIcon(pm);
            icon = resources.getDrawable(applicationInfo.icon, null);
        } catch (Exception e) {
            print("getBitmap package error:" + applicationInfo.packageName);
            Log.e("Nightmare", "getBitmap package error:" + applicationInfo.packageName);
//            e.printStackTrace();
            return null;
        }
        try {
            if (icon == null) {
                print(applicationInfo.packageName + "icon null");
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
            print("Exception:" + e);
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


    void creatVirtualDisplay() {
        final int callingUid = Binder.getCallingUid();
        String[] packageNames = context.getPackageManager().getPackagesForUid(callingUid);
        print("packageNames" + Arrays.toString(packageNames));
        print("packageNames" + context.getPackageName());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            MediaProjectionManager mProjectionManager = (MediaProjectionManager) this.context.getSystemService(MEDIA_PROJECTION_SERVICE);
//
//            IInterface iInterface = serviceManager.getService("media_projection", "android.media.projection.IMediaProjection");
//            Intent intent = new Intent();
//            Class<?> intentClazz = null;
//            try {
//                intentClazz = Class.forName("android.content.Intent");
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//            Method putExtra = null;
//            try {
//                putExtra = intentClazz.getDeclaredMethod("putExtra", String.class, IBinder.class);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//            try {
//                putExtra.invoke(intent, "android.media.projection.extra.EXTRA_MEDIA_PROJECTION", iInterface.asBinder());
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//            Class<?> cls = null;
//            try {
//                cls = Class.forName("android.media.projection.MediaProjection");
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//            Constructor<?> activityThreadConstructor = null;
//            try {
//                activityThreadConstructor = cls.getDeclaredConstructor(Context.class, Class.forName("android.media.projection.IMediaProjection"));
//                activityThreadConstructor.setAccessible(true);
////                ReflectUtil.listAllObject(activityThreadConstructor.getClass());
//            } catch (NoSuchMethodException | ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//            try {
//                Object activityThread = activityThreadConstructor.newInstance(context, iInterface.asBinder());
////                ReflectUtil.listAllObject(activityThread.getClass());
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
        }
        WindowManager windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        print("初始化windowManager" + windowManager);
        Display mDisplay = windowManager.getDefaultDisplay();
        float refreshRate = mDisplay.getRefreshRate();
        final DisplayMetrics metrics = new DisplayMetrics();
//        // use getMetrics is 2030, use getRealMetrics is 2160, the diff is NavigationBar's height
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mDisplay.getRealMetrics(metrics);
        }
        int mWidth = metrics.widthPixels;//size.x;
        int mHeight = metrics.heightPixels;//size.y;
        DisplayManager displayManager = null;
        print("准备创建");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            displayManager = (DisplayManager) this.context.getSystemService(Context.DISPLAY_SERVICE);
            print(">>>" + displayManager);
//        SurfaceView surfaceView = new SurfaceView(this);
            SurfaceTexture texture = new SurfaceTexture(textureID);
            textureID++;
            print("准备创建1");
            Surface surface = new Surface(texture);
            print("准备创建2");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                VirtualDisplay mVirtualDisplay = displayManager.createVirtualDisplay(
                        "uncon-vd",
                        400,
                        400,
                        400,
                        surface,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                                DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION |
                                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC |
                                1 << 7
                );
            }
            print("准备创建3");
        }
    }

}
