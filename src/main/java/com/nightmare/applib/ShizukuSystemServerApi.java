package com.nightmare.applib;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.hardware.display.IDisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;

public class ShizukuSystemServerApi {

    private static final Singleton<IPackageManager> PACKAGE_MANAGER = new Singleton<IPackageManager>() {
        @Override
        protected IPackageManager create() {
            return IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        }
    };


    public static final Singleton<IActivityManager> ACTIVITY_MANAGER = new Singleton<IActivityManager>() {
        @Override
        protected IActivityManager create() {
            IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                return IActivityManager.Stub.asInterface(binder);
            } else {
                return ActivityManagerNative.asInterface(binder);
            }
        }
    };


//    public static final Singleton<DisplayManager> DISPLAY_MANAGER = new Singleton<DisplayManager>() {
//        /** @noinspection JavaReflectionMemberAccess*/
//        @Override
//        protected DisplayManager create() {
            // old way
//            try {
//                Method asInterfaceMethod = Class.forName("android.hardware.display.IDisplayManager$Stub").getMethod("asInterface", IBinder.class);
//                // Get IBinder from shizuku
//                IBinder binder = SystemServiceHelper.getSystemService(Context.DISPLAY_SERVICE);
//                // I don't know what this is for
//                IBinder binderWrapper = new ShizukuBinderWrapper(binder);
//                // idm is IDisplayManager
//                IInterface idm = (IInterface) asInterfaceMethod.invoke(null, binderWrapper);
//
//                // Get DisplayManagerGlobal
//                // Need invoke HiddenApiBypass.addHiddenApiExemptions("L"); before this
//                Class<?> clazz = Class.forName("android.hardware.display.DisplayManagerGlobal");
//                Constructor<?> constructor = clazz.getDeclaredConstructor(Class.forName("android.hardware.display.IDisplayManager"));
//                constructor.setAccessible(true);
//                Object dmg = constructor.newInstance(idm);
//
//                // Notice should use FakeContext.get() instead of ApplicationUtils.getApplication()
//                // FakeContext.get().getPackageName() will return "com.android.shell"
//                DisplayManager displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
//                @SuppressLint("SoonBlockedPrivateApi") Field mGlobal = DisplayManager.class.getDeclaredField("mGlobal");
//                mGlobal.setAccessible(true);
//                mGlobal.set(displayManager, dmg);
//            } catch (NoSuchMethodException | ClassNotFoundException | NoSuchFieldException |
//                     InvocationTargetException | IllegalAccessException |
//                     InstantiationException e) {
//                throw new RuntimeException(e);
//            }
//            IInterface idm = DISPLAY_MANAGER_INTERFACE.get();
//            try {
//                Class<?> clazz = Class.forName("android.hardware.display.DisplayManagerGlobal");
//                Class<?> idmClazz = Class.forName("android.hardware.display.IDisplayManager");
//                Constructor<?> constructor = clazz.getDeclaredConstructor(idmClazz);
//                constructor.setAccessible(true);
//                Object dmg = constructor.newInstance(idm);
//                Log.d("DisplayManager", "dmg:" + dmg);
//                DisplayManager displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
//                @SuppressLint("SoonBlockedPrivateApi") Field mGlobal = DisplayManager.class.getDeclaredField("mGlobal");
//                mGlobal.setAccessible(true);
//                mGlobal.set(displayManager, dmg);
//                return displayManager;
//            } catch (ClassNotFoundException | NoSuchFieldException | InvocationTargetException |
//                     NoSuchMethodException | IllegalAccessException | InstantiationException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    };
}

