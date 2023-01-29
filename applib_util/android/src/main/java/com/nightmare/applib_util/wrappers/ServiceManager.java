package com.nightmare.applib_util.wrappers;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressLint("PrivateApi,DiscouragedPrivateApi")
// reflect ServiceManager
public final class ServiceManager {

    public static final String PACKAGE_NAME = "com.android.shell";
    public static final int USER_ID = 0;

    private final Method getServiceMethod;

    private IPackageManager packageManager;
    private ActivityManager activityManager;
    private DisplayManager displayManager;

    public ServiceManager() {
        try {
            getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public IInterface getService(String service, String type) {
        try {
            IBinder binder = (IBinder) getServiceMethod.invoke(null, service);
            Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public DisplayManager getDisplayManager() {
        if (displayManager == null) {
            displayManager = new DisplayManager(getService("display", "android.hardware.display.IDisplayManager"));
//            displayManager = new DisplayManager(getService("display", "android.hardware.display.DisplayManagerGlobal "));
//            IInterface iInterface = getService("media_projection", "android.media.projection.IMediaProjection");
////            ReflectUtil.listAllObject(iInterface.getClass());
//            Class<?> cls = null;
//            try {
//                cls = Class.forName("android.hardware.display.DisplayManagerGlobal");
//                Method getDefaultMethod = cls.getDeclaredMethod("getInstance");
//                Object appBindData = getDefaultMethod.invoke(null);
//
//                displayManager=new DisplayManager(am);
//            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//                e.printStackTrace();
//            }
        }
        return displayManager;
    }

    public IPackageManager getPackageManager() {
        if (packageManager == null) {
            packageManager = new IPackageManager(getService("package", "android.content.pm.IPackageManager"));
        }
        return packageManager;
    }

    public ActivityManager getActivityManager() {
        if (activityManager == null) {
            try {
                // On old Android versions, the ActivityManager is not exposed via AIDL,
                // so use ActivityManagerNative.getDefault()
                Class<?> cls = Class.forName("android.app.ActivityManagerNative");
                Method getDefaultMethod = cls.getDeclaredMethod("getDefault");
                IInterface am = (IInterface) getDefaultMethod.invoke(null);
                activityManager = new ActivityManager(am);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        return activityManager;
    }
}