package com.nightmare.applib.wrappers;


import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Handler;
import android.view.Display;
import android.view.Surface;

import com.nightmare.applib.FakeContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@SuppressLint("PrivateApi,DiscouragedPrivateApi")
public final class DisplayManager {
    private final Object manager; // instance of hidden class android.hardware.display.DisplayManagerGlobal
    private Method createVirtualDisplayMethod;

    static DisplayManager create() {
        try {
            Class<?> clazz = Class.forName("android.hardware.display.DisplayManagerGlobal");
            Method getInstanceMethod = clazz.getDeclaredMethod("getInstance");
            Object dmg = getInstanceMethod.invoke(null);
            return new DisplayManager(dmg);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private DisplayManager(Object manager) {
        this.manager = manager;
    }


    private Object createVirtualDisplayConfig(
            String name,
            int width,
            int height,
            int density,
            int flags,
            Surface surface
    )
            throws IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        Class<?> configClass = Class.forName("android.hardware.display.VirtualDisplayConfig$Builder");
        Constructor<?> constructor = configClass.getConstructor(String.class, int.class, int.class, int.class);
        Object builder = constructor.newInstance(name, width, height, density);
        configClass.getMethod("setFlags", int.class).invoke(builder, flags);
        configClass.getMethod("setSurface", Surface.class).invoke(builder, surface);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            configClass.getMethod("setRequestedRefreshRate", float.class).invoke(builder, 120f);
        }
        return builder.getClass().getMethod("build").invoke(builder);
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int density, Surface surface, int flags)
            throws NoSuchMethodException, ClassNotFoundException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InstantiationException, InvocationTargetException {
        try {
            // Android 10
            Method createVirtualDisplay = manager.getClass().getMethod("createVirtualDisplay",
                    Context.class,
                    MediaProjection.class,
                    String.class,
                    int.class,
                    int.class,
                    int.class,
                    Surface.class,
                    int.class,
                    Class.forName("android.hardware.display.VirtualDisplay$Callback"),
                    Handler.class,
                    String.class);
            Object virtualDisplay = createVirtualDisplay.invoke(manager, FakeContext.get(), null, name, width, height, density,
                    surface,
                    flags, null, null, null);
            Method getDisplay = Class.forName("android.hardware.display.VirtualDisplay").getMethod("getDisplay");
            Display display = (Display) getDisplay.invoke(virtualDisplay);
            return (VirtualDisplay) virtualDisplay;
        } catch (NoSuchMethodException e) {
        }

        Object config = createVirtualDisplayConfig(
                name,
                width,
                height,
                density,
                flags,
                surface);

        try {
            // Android 12
            Method createVirtualDisplay = manager.getClass().getMethod("createVirtualDisplay",
                    Context.class,
                    MediaProjection.class,
                    Class.forName("android.hardware.display.VirtualDisplayConfig"),
                    Class.forName("android.hardware.display.VirtualDisplay$Callback"),
                    Handler.class);
            Object virtualDisplay = createVirtualDisplay.invoke(manager, FakeContext.get(), null, config, null, null);
            Method getDisplay = Class.forName("android.hardware.display.VirtualDisplay").getMethod("getDisplay");
            Display display = (Display) getDisplay.invoke(virtualDisplay);
            return (VirtualDisplay) virtualDisplay;
        } catch (NoSuchMethodException e) {
        }
//        ReflectUtil.listAllObject(manager);
        try {
            // Android 13
            Method createVirtualDisplay = manager.getClass().getMethod("createVirtualDisplay",
                    Context.class,
                    MediaProjection.class,
                    Class.forName("android.hardware.display.VirtualDisplayConfig"),
                    Class.forName("android.hardware.display.VirtualDisplay$Callback"),
                    Executor.class,
                    Context.class);
            Object virtualDisplay = createVirtualDisplay.invoke(manager, FakeContext.get(), null, config, null, null, FakeContext.get());
            Method getDisplay = Class.forName("android.hardware.display.VirtualDisplay").getMethod("getDisplay");
            Display display = (Display) getDisplay.invoke(virtualDisplay);
            return (VirtualDisplay) virtualDisplay;
        } catch (NoSuchMethodException e) {
        }

        try {
            // Android 14
            Method createVirtualDisplay = manager.getClass().getMethod("createVirtualDisplay",
                    Context.class,
                    MediaProjection.class,
                    Class.forName("android.hardware.display.VirtualDisplayConfig"),
                    Class.forName("android.hardware.display.VirtualDisplay$Callback"),
                    Executor.class);
            Object virtualDisplay = createVirtualDisplay.invoke(manager, FakeContext.get(), null, config, null, null);
            Method getDisplay = Class.forName("android.hardware.display.VirtualDisplay").getMethod("getDisplay");
            Display display = (Display) getDisplay.invoke(virtualDisplay);
            return (VirtualDisplay) virtualDisplay;
        } catch (NoSuchMethodException e) {
        }
        return null;
    }
}
