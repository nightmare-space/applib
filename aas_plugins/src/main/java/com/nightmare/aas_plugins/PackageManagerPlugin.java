package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.ContextStore;
import com.nightmare.aas.helper.L;
import com.nightmare.aas_plugins.util.BitmapHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import fi.iki.elonen.NanoHTTPD;

public class PackageManagerPlugin extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "/package_manager";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String action = session.getParms().get("action");
        assert action != null;
        if (action.equals("get_permissions")) {
            String packageName = session.getParms().get("package");
            String permissions = getAppPermissions(packageName);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", permissions);
        } else if (action.equals("get_icon")) {
            String path = session.getParms().get("path");
            L.d("icon get path -> " + path);
            byte[] bytes = null;
            if (path != null) {
                try {
                    bytes = getApkBitmapBytes(path);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String packageName = session.getParms().get("package");
                L.d("icon get package -> " + packageName);
                assert packageName != null;
                if (packageName.contains(".png")) {
                    int dotIndex = packageName.lastIndexOf('.'); // 找到 '.' 的位置
                    String result;
                    if (dotIndex != -1) {
                        result = packageName.substring(0, dotIndex); // 获取 '.' 前的部分
                    } else {
                        result = packageName; // 如果没有 '.'，返回原字符串
                    }
                    packageName = result;
                }
                L.d("package -> " + packageName);
                bytes = BitmapHelper.bitmap2Bytes(getBitmap(packageName));
            }
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "image/png", new ByteArrayInputStream(bytes), bytes.length);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{}");
    }


    public String getAppPermissions(String packageName) {
        PackageManager packageManager = ContextStore.getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] permissions = packageInfo.requestedPermissions;
//            L.d("permissions: " + permissions);
            if (permissions == null) {
                return "[]";
            }
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonObject.put("datas", jsonArray);
            for (String permission : permissions) {
                JSONObject object = new JSONObject();
                object.put("name", permission);
                try {
                    PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);
//                    L.d("permissionInfo: " + permissionInfo);
                    if (permissionInfo != null) {
                        CharSequence description = permissionInfo.loadDescription(packageManager);
                        if (description != null) {
                            object.put("description", description.toString());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                jsonArray.put(object);
            }
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }


    static public PackageInfo getPackageInfo(String packageName, int flag) {
        PackageManager pm = ContextStore.getContext().getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(packageName, flag);
        } catch (PackageManager.NameNotFoundException e) {
            L.e(packageName + "not found");
        }
        return info;
    }

    static public PackageInfo getPackageInfo(String packageName) {
        return getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
    }

    public Bitmap getBitmap(String packageName) {
        return getBitmap(packageName, false);
    }


    public byte[] getApkBitmapBytes(String path) throws
            InvocationTargetException, IllegalAccessException {
        return BitmapHelper.bitmap2Bytes(getUninstallAPKIcon(path));
    }


    AssetManager getAssetManagerFromPath(String path) {
        AssetManager assetManager = null;
        try {
            assetManager = AssetManager.class.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        try {
            assert assetManager != null;
            assetManager.getClass().getMethod("addAssetPath", String.class).invoke(assetManager, path);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return assetManager;
    }

    /**
     * @param packageName: App package name
     * @return Bitmap
     */
    public synchronized Bitmap getBitmap(String packageName, boolean useDesperateWay) {
        Drawable icon = null;
        PackageInfo packageInfo = getPackageInfo(packageName);
        if (packageInfo == null) {

            L.d(packageName + " packageInfo is null");
            return null;
        }
        L.d("package info -> " + packageInfo);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if (applicationInfo == null) {
            L.d("applicationInfo is null");
            return null;
        }
        try {
            // this way will crash on meizu 21
            // 而且很奇怪，icon 在 crash 的时候，并不为 null，所以还不能直接通过异常处理的方式来切换方案
            // 所以先用之前的方案
            // java.lang.NullPointerException: Attempt to invoke virtual method 'boolean java.lang.String.equals(java.lang.Object)' on a null object reference
            //        at android.content.res.flymetheme.FlymeThemeHelper.makeThemeIcon(FlymeThemeHelper.java:837)
            //        at android.content.res.flymetheme.FlymeThemeHelper.makeThemeIcon(FlymeThemeHelper.java:810)
            //        at android.app.ApplicationPackageManager$Injector.makeThemeIcon(ApplicationPackageManager.java:4035)
            //        at android.app.ApplicationPackageManager.getDrawable(ApplicationPackageManager.java:1817)
            //        at android.app.ApplicationPackageManager.loadUnbadgedItemIcon(ApplicationPackageManager.java:3397)
            //        at android.app.ApplicationPackageManager.loadItemIcon(ApplicationPackageManager.java:3376)
            //        at android.content.pm.PackageItemInfo.loadIcon(PackageItemInfo.java:273)
            //        at com.nightmare.aas_plugins.IconHandler.getBitmap(IconHandler.java:107)
            //        at com.nightmare.aas_plugins.IconHandler.getBitmap(IconHandler.java:83)
            //        at com.nightmare.aas_plugins.IconHandler.handle(IconHandler.java:59)
            //        at com.nightmare.applib.AppServer.serve(AppServer.java:388)
            //        at fi.iki.elonen.NanoHTTPD$HTTPSession.execute(NanoHTTPD.java:945)
            //        at fi.iki.elonen.NanoHTTPD$ClientHandler.run(NanoHTTPD.java:192)
            //        at java.lang.Thread.run(Thread.java:1012)
//            icon = applicationInfo.loadIcon(FakeContext.get().getPackageManager());
//            L.e("applicationInfo.loadIcon failed for " + packageName + " use the second way");
            AssetManager assetManager = AssetManager.class.newInstance();
            //noinspection JavaReflectionMemberAccess
            assetManager.getClass().getMethod("addAssetPath", String.class).invoke(assetManager, applicationInfo.sourceDir);
            Resources resources = new Resources(assetManager, null, null);
            icon = resources.getDrawable(applicationInfo.icon, null);
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
                int w = icon.getIntrinsicWidth();
                int h = icon.getIntrinsicHeight();
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                //设置画布的范围
                icon.setBounds(0, 0, w, h);
                icon.draw(canvas);
                return bitmap;
            }
        } catch (Throwable t) {
            L.e("IconHandler getBitmap Exception:" + t);
            return null;
        }
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
        Drawable icon = getApkIcon(ContextStore.getContext(), apkPath);
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
}
