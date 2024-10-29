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
import com.nightmare.applib.Size;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // public to call it from unit tests
    public static DisplayInfo parseDisplayInfo(String dumpsysDisplayOutput, int displayId) {
        Pattern regex = Pattern.compile(
                "^    mOverrideDisplayInfo=DisplayInfo\\{\".*?, displayId " + displayId + ".*?(, FLAG_.*)?, real ([0-9]+) x ([0-9]+).*?, "
                        + "rotation ([0-9]+).*?, density ([0-9]+).*?, layerStack ([0-9]+)",
                Pattern.MULTILINE);
        Matcher m = regex.matcher(dumpsysDisplayOutput);
        if (!m.find()) {
            return null;
        }
        int flags = parseDisplayFlags(m.group(1));
        int width = Integer.parseInt(m.group(2));
        int height = Integer.parseInt(m.group(3));
        int rotation = Integer.parseInt(m.group(4));
        int density = Integer.parseInt(m.group(5));
        int layerStack = Integer.parseInt(m.group(6));

        return new DisplayInfo(displayId, new Size(width, height), rotation, layerStack, flags, density);
    }

    private static DisplayInfo getDisplayInfoFromDumpsysDisplay(int displayId) {
        try {
            String dumpsysDisplayOutput = Command.execReadOutput("dumpsys", "display");
            return parseDisplayInfo(dumpsysDisplayOutput, displayId);
        } catch (Exception e) {
            Ln.e("Could not get display info from \"dumpsys display\" output", e);
            return null;
        }
    }

    private static int parseDisplayFlags(String text) {
        Pattern regex = Pattern.compile("FLAG_[A-Z_]+");
        if (text == null) {
            return 0;
        }

        int flags = 0;
        Matcher m = regex.matcher(text);
        while (m.find()) {
            String flagString = m.group();
            try {
                Field filed = Display.class.getDeclaredField(flagString);
                flags |= filed.getInt(null);
            } catch (ReflectiveOperationException e) {
                // Silently ignore, some flags reported by "dumpsys display" are @TestApi
            }
        }
        return flags;
    }

    public DisplayInfo getDisplayInfo(int displayId) {
        try {
            Object displayInfo = manager.getClass().getMethod("getDisplayInfo", int.class).invoke(manager, displayId);
            if (displayInfo == null) {
                // fallback when displayInfo is null
                return getDisplayInfoFromDumpsysDisplay(displayId);
            }
            Class<?> cls = displayInfo.getClass();
            // width and height already take the rotation into account
            int width = cls.getDeclaredField("logicalWidth").getInt(displayInfo);
            int height = cls.getDeclaredField("logicalHeight").getInt(displayInfo);
            int rotation = cls.getDeclaredField("rotation").getInt(displayInfo);
            int layerStack = cls.getDeclaredField("layerStack").getInt(displayInfo);
            int flags = cls.getDeclaredField("flags").getInt(displayInfo);
            int dpi = cls.getDeclaredField("logicalDensityDpi").getInt(displayInfo);
            return new DisplayInfo(displayId, new Size(width, height), rotation, layerStack, flags, dpi);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public int[] getDisplayIds() {
        try {
            return (int[]) manager.getClass().getMethod("getDisplayIds").invoke(manager);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private Method getCreateVirtualDisplayMethod() throws NoSuchMethodException {
        if (createVirtualDisplayMethod == null) {
            createVirtualDisplayMethod = android.hardware.display.DisplayManager.class
                    .getMethod("createVirtualDisplay", String.class, int.class, int.class, int.class, Surface.class);
        }
        return createVirtualDisplayMethod;
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int displayIdToMirror, Surface surface) throws Exception {
        Method method = getCreateVirtualDisplayMethod();
        return (VirtualDisplay) method.invoke(null, name, width, height, displayIdToMirror, surface);
    }

    public VirtualDisplay createNewVirtualDisplay(String name, int width, int height, int dpi, Surface surface, int flags) throws Exception {
        Constructor<android.hardware.display.DisplayManager> ctor = android.hardware.display.DisplayManager.class.getDeclaredConstructor(
                Context.class);
        ctor.setAccessible(true);
        android.hardware.display.DisplayManager dm = ctor.newInstance(FakeContext.get());
        return dm.createVirtualDisplay(name, width, height, dpi, surface, flags);
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
