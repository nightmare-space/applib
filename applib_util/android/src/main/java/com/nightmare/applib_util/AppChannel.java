package com.nightmare.applib_util;

import android.annotation.SuppressLint;
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


import com.nightmare.applib_util.wrappers.IPackageManager;
import com.nightmare.applib_util.wrappers.ServiceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
    IPackageManager pm;
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
        @SuppressLint("PrivateApi")
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Constructor<?> activityThreadConstructor = activityThreadClass.getDeclaredConstructor();
        activityThreadConstructor.setAccessible(true);
        Object activityThread = activityThreadConstructor.newInstance();
        @SuppressLint("DiscouragedPrivateApi")
        Method getSystemContextMethod = activityThreadClass.getDeclaredMethod("getSystemContext");
        return (Context) getSystemContextMethod.invoke(activityThread);
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
                    // ??????????????????app????????????
                    PackageInfo withoutHidePackage = pm.getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
//                    Log.w("Nightmare", withoutHidePackage.applicationInfo.loadLabel(context.getPackageManager()) + "");
                    builder.append("\r").append(false);
                } catch (InvocationTargetException e) {
                    print(packageInfo.packageName + "?????????app");
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

    // ?????????crash
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
                // ???????????????????????????
                if (resultTag <= 0) {
                    // <=0????????????????????????
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
                // ??????????????????app??????????????????????????????????????????????????????app??????????????????
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
        AssetManager assetManager = getAssetManagerFromPath(info.sourceDir);
        Resources resources = new Resources(assetManager, displayMetrics, configuration);
        int res = info.labelRes;
        if (info.nonLocalizedLabel != null) {
            return (String) info.nonLocalizedLabel;
        }
        if (res != 0) {
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

                    //???????????????????????????,???:android.permission.INTERNET
//                    print("usesPermissionName=" + usesPermissionName);
                    builder.append(usesPermissionName);
                    //??????usesPermissionName??????????????????????????????
                    PermissionInfo permissionInfo = pm.getPermissionInfo(usesPermissionName, 0);

                    //????????????????????????????????????,???:????????????
//                PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
//                System.out.println("permissionGroup=" + permissionGroupInfo.loadLabel(packageManager).toString());

                    //??????????????????????????????,??????:???????????????????????????
                    String permissionLabel = getLabel(packageInfo.applicationInfo);
//                    print("permissionLabel=" + permissionLabel);

                    //????????????????????????????????????,??????:??????????????????????????????????????????????????????????????????
                    //????????????????????????????????????????????????????????????????????????,?????????????????????????????????????????????????????????.
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

    public String getAppMainActivity(String packageName) {
        StringBuilder builder = new StringBuilder();
//        try {
//            Class<?> cls = Class.forName("android.app.ApplicationPackageManager");
//            ReflectUtil.listAllObject(cls);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        // ReflectUtil.listAllObject(serviceManager.getPackageManager().manager.getClass());
        if (context == null) {
            try {
                Looper.prepare();
                context = getContextWithoutActivity();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                builder.append(launchIntent.getComponent().getClassName());
            } else {
                print(packageName + "????????????Activity??????");
            }
            return builder.toString();
        }
        // ?????????????????????????????????
        // ReflectUtil.listAllObject(serviceManager.getPackageManager().manager.getClass());
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
        Drawable icon = getApkIcon(context,apkPath);
//        try {
//            // apk??????????????????
//            // ????????????Package ?????????, ????????????
//            // ?????????????????????????????????, apk???????????????
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
//            // ???????????????????????????, ???????????????????????????????????????, ???????????????????????????
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
//            // ?????????????????????, ???????????????, ??????????????????, ???????????????
//            // ApplicationInfo info = mPkgInfo.applicationInfo;
//            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
//            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
//            // uid ?????????"-1"??????????????????????????????????????????Uid???
//            Log.d("ANDROID_LAB", "pkg:" + info.packageName + " uid=" + info.uid);
//            AssetManager assetManager = getAssetManagerFromPath(info.sourceDir);
//            Resources resources = new Resources(assetManager, displayMetrics, configuration);
//            CharSequence label = null;
//            if (info.labelRes != 0) {
//                label = resources.getText(info.labelRes);
//            }
//            Log.d("ANDROID_LAB", "label=" + label);
//            // ????????????????????????apk???????????????
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
        PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if (applicationInfo == null) {
            print("applicationInfo == null");
            return null;
        }
        AssetManager assetManager = getAssetManagerFromPath(applicationInfo.sourceDir);
        Resources resources = new Resources(assetManager, displayMetrics, configuration);
        Drawable icon;
        try {
            icon = resources.getDrawable(applicationInfo.icon, null);
        } catch (Exception e) {
            Log.e("Nightmare", "getBitmap package error:" + applicationInfo.packageName);
//            e.printStackTrace();
            return null;
        }
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
//
//    public static Drawable getDrawable(Context context, int id) {
//        if (Build.VERSION.SDK_INT >= 21) {
//            return context.getDrawable(id);
//        } else if (Build.VERSION.SDK_INT >= 16) {
//            return context.getResources().getDrawable(id);
//        }
//        return null;
//    }

}
