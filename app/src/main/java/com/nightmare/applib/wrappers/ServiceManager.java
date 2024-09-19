package com.nightmare.applib.wrappers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

import com.nightmare.applib.FakeContext;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.ReflectUtil;

import java.lang.reflect.InvocationTargetException;
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

    //    private static WindowManager windowManager;
    private static DisplayManagerRef displayManagerRef;
    private static IPackageManager packageManager;
    private static InputManagerSimulate inputManager;
    //    private static PowerManager powerManager;
//    private static StatusBarManager statusBarManager;
//    private static ClipboardManager clipboardManager;
    private static ActivityManager activityManager;

    private ServiceManager() {
        /* not instantiable */
    }

    private static IInterface getService(String service, String type) {
        try {
            IBinder binder = (IBinder) GET_SERVICE_METHOD.invoke(null, service);
            Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

//    public static WindowManager getWindowManager() {
//        if (windowManager == null) {
//            windowManager = new WindowManager(getService("window", "android.view.IWindowManager"));
//        }
//        return windowManager;
//    }

    public static DisplayManagerRef getDisplayManager() {
        if (displayManagerRef == null) {
            try {
                Class<?> clazz = Class.forName("android.hardware.display.DisplayManagerGlobal");
                Method getInstanceMethod = clazz.getDeclaredMethod("getInstance");
                Object dmg = getInstanceMethod.invoke(null);
                ReflectUtil.listAllObject(dmg);
                displayManagerRef = new DisplayManagerRef(dmg);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new AssertionError(e);
            }
        }
        return displayManagerRef;
    }

    //    public static InputManager getInputManager() {
////        if (inputManager == null) {
////            try {
////                Method getInstanceMethod = android.hardware.input.InputManager.class.getDeclaredMethod("getInstance");
////                android.hardware.input.InputManager im = (android.hardware.input.InputManager) getInstanceMethod.invoke(null);
////                inputManager = new InputManager(im);
////            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
////                throw new AssertionError(e);
////            }
////        }
////        return inputManager;
////    }

    public static Class<?> getInputManagerClass() {
        try {
            // Parts of the InputManager class have been moved to a new InputManagerGlobal class in Android 14 preview
            return Class.forName("android.hardware.input.InputManagerGlobal");
        } catch (ClassNotFoundException e) {
            return android.hardware.input.InputManager.class;
        }
    }

    public static InputManagerSimulate getInputManager() {
        if (inputManager == null) {
            try {
                Class<?> inputManagerClass = getInputManagerClass();
                Method getInstanceMethod = inputManagerClass.getDeclaredMethod("getInstance");
                Object im = getInstanceMethod.invoke(null);
//                ReflectUtil.listAllObject(im);
//                // 反射调用 void requestPointerCapture(interface android.os.IBinder arg0,boolean arg1)
//                Method requestPointerCaptureMethod = inputManagerClass.getDeclaredMethod("requestPointerCapture", IBinder.class, boolean.class);
//                requestPointerCaptureMethod.invoke(im, new Binder(), true);
////                ReflectUtil.listAllObject(im);
//                // 反射调用 void setPointerIconType(int arg0)
//                Method setPointerIconTypeMethod = inputManagerClass.getDeclaredMethod("setPointerIconType", int.class);
//                setPointerIconTypeMethod.invoke(im, 0);
//                // 反射调用 getMousePointerSpeed()
//
//                InputManager imm = (InputManager) FakeContext.get().getSystemService(Context.INPUT_SERVICE);
//                ReflectUtil.listAllObject(imm);
//                Method getMousePointerSpeedMethod = imm.getClass().getDeclaredMethod("getMousePointerSpeed");
//                getMousePointerSpeedMethod.setAccessible(true);
//                int speed =(int) getMousePointerSpeedMethod.invoke(imm);
//                L.d("speed: " + speed);

                // 反射调用 void pilferPointers(interface android.os.IBinder arg0)
//                Method pilferPointersMethod = inputManagerClass.getDeclaredMethod("pilferPointers", IBinder.class);
//                pilferPointersMethod.invoke(im, new Binder());
                inputManager = new InputManagerSimulate(im);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new AssertionError(e);
            }
        }
        return inputManager;
    }

//    public static PowerManager getPowerManager() {
//        if (powerManager == null) {
//            powerManager = new PowerManager(getService("power", "android.os.IPowerManager"));
//        }
//        return powerManager;
//    }

//    public static StatusBarManager getStatusBarManager() {
//        if (statusBarManager == null) {
//            statusBarManager = new StatusBarManager(getService("statusbar", "com.android.internal.statusbar.IStatusBarService"));
//        }
//        return statusBarManager;
//    }

//    public static ClipboardManager getClipboardManager() {
//        if (clipboardManager == null) {
//            IInterface clipboard = getService("clipboard", "android.content.IClipboard");
//            if (clipboard == null) {
//                // Some devices have no clipboard manager
//                // <https://github.com/Genymobile/scrcpy/issues/1440>
//                // <https://github.com/Genymobile/scrcpy/issues/1556>
//                return null;
//            }
//            clipboardManager = new ClipboardManager(clipboard);
//        }
//        return clipboardManager;
//    }

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