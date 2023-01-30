package com.nightmare.applib_util.utils;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.view.SurfaceView;

public class VirtualDisplayUtil {
    public VirtualDisplay createVirtualDisplay(Context context, int width, int height, int densityDpi) {
        SurfaceView surfaceView = new SurfaceView(context);
        DisplayManager
                displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        return displayManager.createVirtualDisplay(
                "uncon-vd",
                width,
                height,
                densityDpi,
                surfaceView.getHolder().getSurface(),
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION |
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | 1 << 7
        );
    }
}
