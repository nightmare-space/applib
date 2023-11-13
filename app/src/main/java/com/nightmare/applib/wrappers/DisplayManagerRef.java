package com.nightmare.applib.wrappers;

import com.nightmare.applib.utils.L;

import android.view.Display;
import android.view.Surface;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.projection.MediaProjection;
import android.os.Handler;

import java.lang.reflect.Method;

public final class DisplayManagerRef {
    private final Object manager; // instance of hidden class android.hardware.display.DisplayManagerGlobal

    public DisplayManagerRef(Object manager) {
        this.manager = manager;
    }


    public int[] getDisplayIds() {
        try {
            return (int[]) manager.getClass().getMethod("getDisplayIds").invoke(manager);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private Object createVirtualDisplayConfig(String name, int width, int height, int density, int flags,
                                              Surface surface, String uniqueId, int displayIdToMirror, boolean windowManagerMirroring)
            throws IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        Object builder = Class.forName("android.hardware.display.VirtualDisplayConfig$Builder")
                .getConstructor(String.class, int.class, int.class, int.class)
                .newInstance(name, width, height, density);
        builder.getClass().getMethod("setFlags", int.class).invoke(builder, flags);
        builder.getClass().getMethod("setSurface", Surface.class).invoke(builder, surface);
        return builder.getClass().getMethod("build").invoke(builder);
    }

    class FakePackageNameContext extends ContextWrapper {
        public FakePackageNameContext() {
            super(null);
        }

        @Override
        public String getPackageName() {
            // `Workarounds.getContext().getPackageName()` always returns `android`,
            // but `createVirtualDisplay` will validate the package name againest current
            // uid.
            // For ADB shell, the uid is 2000 (shell) and the only avaiable package name is
            // `com.android.shell`
            return "com.android.shell";
        }

        @Override
        public Display getDisplay() {
            return null;
        }
    }

    public Display createVirtualDisplay(Surface surface, int width, int height, int density)
            throws NoSuchMethodException, ClassNotFoundException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        FakePackageNameContext wrapper = new FakePackageNameContext();
        L.d("Package name: " + wrapper.getPackageName());

        String name = "scrcpy-virtual";
        int VIRTUAL_DISPLAY_FLAG_PUBLIC = 1 << 0;
        int VIRTUAL_DISPLAY_FLAG_PRESENTATION = 1 << 1;
        int VIRTUAL_DISPLAY_FLAG_SECURE = 1 << 2;
        int VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY = 1 << 3;
        int VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR = 1 << 4;
        int VIRTUAL_DISPLAY_FLAG_CAN_SHOW_WITH_INSECURE_KEYGUARD = 1 << 5;
        int VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH = 1 << 6;
        int VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT = 1 << 7;
        int VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL = 1 << 8;
        int VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS = 1 << 9;
        int VIRTUAL_DISPLAY_FLAG_TRUSTED = 1 << 10;
        int flags = VIRTUAL_DISPLAY_FLAG_PRESENTATION
                | VIRTUAL_DISPLAY_FLAG_CAN_SHOW_WITH_INSECURE_KEYGUARD
                | VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH
                | VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS;

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
            Object virtualDisplay = createVirtualDisplay.invoke(manager, wrapper, null, name, width, height, density,
                    surface,
                    flags, null, null, null);
            Method getDisplay = Class.forName("android.hardware.display.VirtualDisplay").getMethod("getDisplay");
            Display display = (Display) getDisplay.invoke(virtualDisplay);
            return display;
        } catch (NoSuchMethodException e) {
        }

        Object config = createVirtualDisplayConfig(
                name,
                width,
                height,
                density,
                flags,
                surface,
                null,
                0,
                false);

        try {
            // Android 12
            Method createVirtualDisplay = manager.getClass().getMethod("createVirtualDisplay",
                    Context.class,
                    MediaProjection.class,
                    Class.forName("android.hardware.display.VirtualDisplayConfig"),
                    Class.forName("android.hardware.display.VirtualDisplay$Callback"),
                    Handler.class);
            Object virtualDisplay = createVirtualDisplay.invoke(manager, wrapper, null, config, null, null);
            Method getDisplay = Class.forName("android.hardware.display.VirtualDisplay").getMethod("getDisplay");
            Display display = (Display) getDisplay.invoke(virtualDisplay);
            return display;
        } catch (NoSuchMethodException e) {
        }

        // Android 13
        Method createVirtualDisplay = manager.getClass().getMethod("createVirtualDisplay",
                Context.class,
                MediaProjection.class,
                Class.forName("android.hardware.display.VirtualDisplayConfig"),
                Class.forName("android.hardware.display.VirtualDisplay$Callback"),
                Executor.class,
                Context.class);
        Object virtualDisplay = createVirtualDisplay.invoke(manager, wrapper, null, config, null, null, wrapper);
        Method getDisplay = Class.forName("android.hardware.display.VirtualDisplay").getMethod("getDisplay");
        Display display = (Display) getDisplay.invoke(virtualDisplay);
        return display;
    }
}