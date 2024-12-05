package com.nightmare.aas_plugins.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;

import com.nightmare.aas.foundation.FakeContext;
import com.nightmare.aas.helper.L;
import com.nightmare.aas.helper.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressLint({"PrivateApi", "SoonBlockedPrivateApi", "BlockedPrivateApi"})
@TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public final class DisplayControl {

    private static final Class<?> CLASS;

    static {
        Class<?> displayControlClass = null;
        try {
            Class<?> classLoaderFactoryClass = Class.forName("com.android.internal.os.ClassLoaderFactory");
            Method createClassLoaderMethod = classLoaderFactoryClass.getDeclaredMethod("createClassLoader", String.class, String.class, String.class,
                    ClassLoader.class, int.class, boolean.class, String.class);
            ClassLoader classLoader = (ClassLoader) createClassLoaderMethod.invoke(null, "/system/framework/services.jar", null, null,
                    ClassLoader.getSystemClassLoader(), 0, true, null);

            displayControlClass = classLoader.loadClass("com.android.server.display.DisplayControl");

            Method loadMethod = Runtime.class.getDeclaredMethod("loadLibrary0", Class.class, String.class);
            loadMethod.setAccessible(true);
            loadMethod.invoke(Runtime.getRuntime(), displayControlClass, "android_servers");


            Class cl = classLoader.loadClass("com.android.server.display.DisplayManagerService");
            loadMethod.invoke(Runtime.getRuntime(), cl, "android_servers");
            //  public DisplayManagerService(Context context) {
            //        this(context, new Injector());

            //    }
//            反射构造
            Object dms = cl.getDeclaredConstructor(android.content.Context.class).newInstance(FakeContext.get());

//            public void onBootPhase(int phase) {
            Method onBootPhase = cl.getDeclaredMethod("onBootPhase", int.class);
            onBootPhase.setAccessible(true);
//            onBootPhase.invoke(dms, 100);
            onBootPhase.invoke(dms, 1000);

            Object o = ReflectionHelper.invokeMethod(dms, "getDisplayDeviceInfoInternal", 0);
            L.d("o -> " + o);
            Object mode = ReflectionHelper.invokeMethod(dms, "getActiveDisplayModeAtStart", 0);
            L.d("mode -> " + mode);

//            mVirtualDisplayAdapter
            ReflectionHelper.invokeMethod(dms, "registerDefaultDisplayAdapters");
            Field mVirtualDisplayAdapterF = cl.getDeclaredField("mVirtualDisplayAdapter");
            mVirtualDisplayAdapterF.setAccessible(true);
            Object mVirtualDisplayAdapter = mVirtualDisplayAdapterF.get(dms);
            L.d("mVirtualDisplayAdapter -> " + mVirtualDisplayAdapter);
            Object userMode = ReflectionHelper.invokeMethod(dms, "getUserPreferredDisplayModeInternal", 2);
            L.d("userMode -> " + userMode);
            Display.Mode systemMode = (Display.Mode) ReflectionHelper.invokeMethod(dms, "getSystemPreferredDisplayModeInternal", 0);
            L.d("systemMode -> " + systemMode);
            IBinder token = (IBinder) ReflectionHelper.invokeMethod(dms, "getDisplayToken", 0);
            L.d("token:" + token);
            // void setUserPreferredDisplayModeInternal(int displayId, Display.Mode mode)


            /**
             * No display mode switching will happen.
             * @hide
             */
//            public static final int SWITCHING_TYPE_NONE = 0;

            /**
             * Allow only refresh rate switching between modes in the same configuration group. This way
             * only switches without visual interruptions for the user will be allowed.
             * @hide
             */
//            public static final int SWITCHING_TYPE_WITHIN_GROUPS = 1;

            /**
             * Allow refresh rate switching between all refresh rates even if the switch with have visual
             * interruptions for the user.
             * @hide
             */
//            public static final int SWITCHING_TYPE_ACROSS_AND_WITHIN_GROUPS = 2;

            /**
             * Allow render frame rate switches, but not physical modes.
             * @hide
             */
//            public static final int SWITCHING_TYPE_RENDER_FRAME_RATE_ONLY = 3;
            ReflectionHelper.invokeMethod(dms, "setRefreshRateSwitchingTypeInternal", 2);
            int type = (int) ReflectionHelper.invokeMethod(dms, "getRefreshRateSwitchingTypeInternal");
            L.d("type -> " + type);
            ReflectionHelper.invokeMethod(dms, "setShouldAlwaysRespectAppRequestedModeInternal", true);
            boolean should = (boolean) ReflectionHelper.invokeMethod(dms, "shouldAlwaysRespectAppRequestedModeInternal");
            L.d("shouldAlwaysRespectAppRequestedMode -> " + should);

            DisplayManager displayManager = null;
            try {
                //noinspection JavaReflectionMemberAccess
                displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            Display[] displayss = displayManager.getDisplays();
            for (Display display : displayss) {
                if (display.getDisplayId() != 0) {
                    Display.Mode[] modes = display.getSupportedModes();
                    for (Display.Mode m : modes) {
                        L.d("mode:" + m);
                        if (m.getRefreshRate() == 120.00001f) {
                            L.d("set mode:" + m);
                            // setUserPreferredDisplayModeInternal
                            Method setUserPreferredDisplayModeInternal1 = cl.getDeclaredMethod("setUserPreferredDisplayModeInternal", int.class, Display.Mode.class);
                            setUserPreferredDisplayModeInternal1.setAccessible(true);
                            setUserPreferredDisplayModeInternal1.invoke(dms, display.getDisplayId(), m);
                        }
                    }
                }
            }
            // getDeviceForDisplayLocked(int arg0)
            Method getDeviceForDisplayLocked = cl.getDeclaredMethod("getDeviceForDisplayLocked", int.class);
            getDeviceForDisplayLocked.setAccessible(true);
            Object device = getDeviceForDisplayLocked.invoke(dms, 0);
            L.d("device:" + device);
//            mode = (Display.Mode) getUserPreferredDisplayModeInternal.invoke(dms, 2);
//            L.d("mode:" + mode);
        } catch (Throwable e) {
            L.d("Could not initialize DisplayControl" + e);
            // Do not throw an exception here, the methods will fail when they are called
        }
        CLASS = displayControlClass;
    }

    private static Method getPhysicalDisplayTokenMethod;
    private static Method getPhysicalDisplayIdsMethod;

    private DisplayControl() {
        // only static methods
    }

    private static Method getGetPhysicalDisplayTokenMethod() throws NoSuchMethodException {
        if (getPhysicalDisplayTokenMethod == null) {
            getPhysicalDisplayTokenMethod = CLASS.getMethod("getPhysicalDisplayToken", long.class);
        }
        return getPhysicalDisplayTokenMethod;
    }

    public static IBinder getPhysicalDisplayToken(long physicalDisplayId) {
        try {
            Method method = getGetPhysicalDisplayTokenMethod();
            return (IBinder) method.invoke(null, physicalDisplayId);
        } catch (ReflectiveOperationException e) {
            L.e("Could not invoke method" + e);
            return null;
        }
    }

    private static Method getGetPhysicalDisplayIdsMethod() throws NoSuchMethodException {
        if (getPhysicalDisplayIdsMethod == null) {
            getPhysicalDisplayIdsMethod = CLASS.getMethod("getPhysicalDisplayIds");
        }
        return getPhysicalDisplayIdsMethod;
    }

    public static long[] getPhysicalDisplayIds() {
        try {
            Method method = getGetPhysicalDisplayIdsMethod();
            return (long[]) method.invoke(null);
        } catch (ReflectiveOperationException e) {
            L.e("Could not invoke method" + e);
            return null;
        }
    }
}
