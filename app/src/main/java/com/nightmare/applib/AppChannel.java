package com.nightmare.applib;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nightmare on 2021/7/29.
 */

public class AppChannel {
    Context context;
    PackageManager pm;

    static final String SOCKET_NAME = "app_manager";
    static final int RANGE_START = 6000;
    static final int RANGE_END = 6010;

    public AppChannel(Context context) {
        this.context = context;
        pm = context.getPackageManager();
    }


    static public byte[] Bitmap2Bytes(Bitmap bm) {
        if (bm == null) {
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static void main(String[] arg) throws Exception {
        Context ctx = getContextWithoutActivity();
        startServer(ctx);
        // 不能让进程退了
        int placeholder = System.in.read();

    }

    public static Context getContextWithoutActivity() throws Exception {
        Looper.prepareMainLooper();
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

    // 更安全的拿到一个ServerSocket
    // 有的时候会端口占用
    public static ServerSocket safeGetServerSocket() {
        for (int i = RANGE_START; i < RANGE_END; i++) {
            try {
                return new ServerSocket(i);
            } catch (IOException e) {
                print("端口" + i + "被占用");
            }
        }
        return null;
    }

    // 返回端口号，最后给客户端连接的
    public static int startServer(Context context) {
        ServerSocket serverSocket = safeGetServerSocket();
        assert serverSocket != null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startServerWithServerSocket(serverSocket, context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        System.out.println("success start:" + serverSocket.getLocalPort());
        System.out.flush();
        return serverSocket.getLocalPort();
    }

    public static void startServerWithServerSocket(ServerSocket serverSocket, Context context) throws IOException {
        while (true) {
            print("等待下次连接");
            Socket socket = serverSocket.accept();
            print("连接成功");
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String data = br.readLine();
            String type = data.replaceAll(":.*", ":");
            print("type:" + type);
            AppChannel appInfo = new AppChannel(context);
            switch (type) {
                case AppChannelProtocol.getIconData:
                    handleIcon(os, context, data.replace(AppChannelProtocol.getIconData, ""));
                    break;
                case AppChannelProtocol.getAllAppInfo:
                    String arg = data.replace(AppChannelProtocol.getAllAppInfo, "");
                    handleAllAppInfo(os, context, arg.equals("1"));
                    break;
                case AppChannelProtocol.getAppInfos:
                    handleAppInfos(os, context, data.replace(AppChannelProtocol.getAppInfos, ""));
                    break;
                case AppChannelProtocol.getAllIconData:
                    handleAllAppIcon(os, context, data.replace(AppChannelProtocol.getAllIconData, ""));
                    break;
                case AppChannelProtocol.getAppActivity:
                    os.write(appInfo.getAppActivitys(data.replace(AppChannelProtocol.getAppActivity, "")).getBytes());
                case AppChannelProtocol.getAppPermissions:
                    os.write(appInfo.getAppPermissions(data.replace(AppChannelProtocol.getAppPermissions, "")).getBytes());
                    break;
                case AppChannelProtocol.getAppDetail:
                    os.write(appInfo.getAppDetail(data.replace(AppChannelProtocol.getAppDetail, "")).getBytes());
                    break;
                case AppChannelProtocol.getAppMainActivity:
                    os.write(appInfo.getAppMainActivity(data.replace(AppChannelProtocol.getAppMainActivity, "")).getBytes());
                    break;
                case AppChannelProtocol.openAppByPackage:
                    appInfo.openApp(data.replace(AppChannelProtocol.openAppByPackage, ""));
                    break;
                case AppChannelProtocol.checkToken:
                    os.write("OK".getBytes());
                    break;
                default:
                    socket.close();
                    return;
            }
            socket.setReuseAddress(true);
            socket.close();
        }
    }

    public static void handleIcon(OutputStream outputStream, Context context, String packageName) throws IOException {
        AppChannel appInfo = new AppChannel(context);
        outputStream.write(appInfo.getBitmapBytes(packageName));
    }

    public static void handleAllAppIcon(OutputStream outputStream, Context context, String data) throws IOException {
        List<String> id = stringToList(data);
        AppChannel appInfo = new AppChannel(context);
        for (String packageName : id) {
            outputStream.write(appInfo.getBitmapBytes(packageName));
            outputStream.flush();
        }
    }

    public static void handleAppInfos(OutputStream outputStream, Context context, String data) throws IOException {
        AppChannel appInfo = new AppChannel(context);
        List<String> packages = stringToList(data);
        outputStream.write(appInfo.getAppInfos(packages).getBytes());
    }

    public static void handleAllAppInfo(OutputStream outputStream, Context context, boolean isSystemApp) throws IOException {
        AppChannel appInfo = new AppChannel(context);
        outputStream.write(appInfo.getAllAppInfo(isSystemApp).getBytes());
    }


    public String getAppInfos(List<String> packages) {
        StringBuilder builder = new StringBuilder();
        for (String packageName : packages) {
            PackageInfo packageInfo = null;
            try {
                packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (packageInfo != null) {
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                builder.append(applicationInfo.packageName);
                builder.append("\r").append(applicationInfo.loadLabel(pm));
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
                } catch (PackageManager.NameNotFoundException e) {
                    builder.append("\r").append(true);
                    print(packageInfo.packageName + "为隐藏app");
                }
                builder.append("\r").append(applicationInfo.uid);
                builder.append("\r").append(applicationInfo.sourceDir);
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    // 这儿有crash
    public String getAllAppInfo(boolean isSystemApp) {
        @SuppressLint("QueryPermissionsNeeded")
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        StringBuilder builder = new StringBuilder();
        for (PackageInfo packageInfo : packages) {
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
            builder.append("\r").append(applicationInfo.loadLabel(pm));
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
            } catch (PackageManager.NameNotFoundException e) {
                builder.append("\r").append(true);
            }
            builder.append("\r").append(applicationInfo.uid);
            builder.append("\r").append(applicationInfo.sourceDir);
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    public void openApp(String packageName) {
        new Thread(() -> {
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName cName = new ComponentName(packageName, getAppMainActivity(packageName));
                intent.setComponent(cName);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String getAppActivitys(String data) {
        StringBuilder builder = new StringBuilder();
        @SuppressLint("QueryPermissionsNeeded")
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo pack : packages) {
            if (pack.packageName.equals(data)) {
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(data, PackageManager.GET_ACTIVITIES);
                    if (packageInfo.activities == null) {
                        return "";
                    }
                    for (ActivityInfo info : packageInfo.activities) {
                        builder.append(info.name).append("\n");
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    return builder.toString();
                }
            }

        }
        return builder.toString();

    }

    public String getAppPermissions(String data) {
        StringBuilder builder = new StringBuilder();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(data, PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
            String[] usesPermissionsArray = packageInfo.requestedPermissions;
            for (String usesPermissionName : usesPermissionsArray) {

                //得到每个权限的名字,如:android.permission.INTERNET
                print("usesPermissionName=" + usesPermissionName);
                builder.append(usesPermissionName);
                //通过usesPermissionName获取该权限的详细信息
                PermissionInfo permissionInfo = pm.getPermissionInfo(usesPermissionName, 0);

                //获得该权限属于哪个权限组,如:网络通信
//                PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
//                System.out.println("permissionGroup=" + permissionGroupInfo.loadLabel(packageManager).toString());

                //获取该权限的标签信息,比如:完全的网络访问权限
                String permissionLabel = permissionInfo.loadLabel(pm).toString();
                print("permissionLabel=" + permissionLabel);

                //获取该权限的详细描述信息,比如:允许该应用创建网络套接字和使用自定义网络协议
                //浏览器和其他某些应用提供了向互联网发送数据的途径,因此应用无需该权限即可向互联网发送数据.
                String permissionDescription = permissionInfo.loadDescription(pm).toString();

                builder.append(" ").append(permissionDescription);
                boolean isHasPermission = PackageManager.PERMISSION_GRANTED == pm.checkPermission(permissionInfo.name, data);
                builder.append(" ").append(isHasPermission).append("\r");
                print("permissionDescription=" + permissionDescription);
                print("===========================================");
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return builder.toString();

    }

    public String getAppMainActivity(String packageName) {
        StringBuilder builder = new StringBuilder();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
        builder.append(launchIntent.getComponent().getClassName());
        // 注释掉的是另外一种方法
//        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        List<ResolveInfo> appList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
//        for (int i = 0; i < appList.size(); i++) {
//            ResolveInfo resolveInfo = appList.get(i);
//            String packageStr = resolveInfo.activityInfo.packageName;
//            if (packageStr.equals(packageName)) {
//                builder.append(resolveInfo.activityInfo.name).append("\n");
//                break;
//            }
//        }
        return builder.toString();
    }

    public String getAppDetail(String data) {
        StringBuilder builder = new StringBuilder();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(data, PackageManager.GET_UNINSTALLED_PACKAGES);
            builder.append(packageInfo.firstInstallTime).append("\r");
            builder.append(packageInfo.lastUpdateTime).append("\r");
            builder.append(packageInfo.applicationInfo.dataDir).append("\r");
            builder.append(packageInfo.applicationInfo.nativeLibraryDir);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }


    private static List<String> stringToList(String strs) {
        String[] str = strs.split(" ");
        return Arrays.asList(str);
    }

    public byte[] getBitmapBytes(String packname) {
        return Bitmap2Bytes(getBitmap(packname));
    }

    public synchronized Bitmap getBitmap(String packname) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = pm.getApplicationInfo(
                    packname, PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (applicationInfo == null) {
            return null;
        }
        Drawable icon = applicationInfo.loadIcon(pm);
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
