package com.nightmare.applib.wrappers;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;
import java.lang.reflect.Method;

@SuppressLint("PrivateApi,DiscouragedPrivateApi")
public final class ServiceManager {

    public static final String PACKAGE_NAME = "com.android.shell";
    public static final int USER_ID = 0;

    private static final Method GET_SERVICE_METHOD;

    static {
        try {
            GET_SERVICE_METHOD = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static DisplayManager displayManager;
    private static IPackageManager packageManager;
    private static InputManager inputManager;
    private static ActivityManager activityManager;

    private ServiceManager() {
        /* not instantiable */
    }

    public static IInterface getService(String service, String type) {
        try {
            IBinder binder = (IBinder) GET_SERVICE_METHOD.invoke(null, service);
            Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static DisplayManager getDisplayManager() {
        if (displayManager == null) {
            displayManager = DisplayManager.create();
        }
        return displayManager;
    }

    public static InputManager getInputManager() {
        if (inputManager == null) {
            inputManager = InputManager.create();
        }
        return inputManager;
    }


    public static ActivityManager getActivityManager() {
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

    public static IPackageManager getPackageManager() {
        if (packageManager == null) {
            packageManager = new IPackageManager(getService("package", "android.content.pm.IPackageManager"));
        }
        return packageManager;
    }
}