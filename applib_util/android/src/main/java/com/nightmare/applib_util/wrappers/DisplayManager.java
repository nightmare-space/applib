package com.nightmare.applib_util.wrappers;

import android.os.IInterface;
import android.view.Display;
import android.view.Surface;

public final class DisplayManager {
    private final IInterface manager;

    public DisplayManager(IInterface manager) {
        this.manager = manager;
//        ReflectUtil.listAllObject(manager.getClass());
    }


    public int[] getDisplayIds() {
        try {
            return (int[]) manager.getClass().getMethod("getDisplayIds").invoke(manager);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public int createVirtualDisplay(String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags) {
//        ReflectUtil.listAllObject(manager.getClass());
//        android.hardware.display.VirtualDisplay
        try {
            return (int) manager.getClass().getMethod("createVirtualDisplay", String.class, String.class, int.class, int.class, int.class, Surface.class, int.class).invoke(manager, new Object[]{packageName, name, width, height, densityDpi, surface, flags});
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
