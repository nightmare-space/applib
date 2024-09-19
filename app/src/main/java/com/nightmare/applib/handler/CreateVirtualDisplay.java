package com.nightmare.applib.handler;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.nightmare.applib.FakeContext;
import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.DisplayUtil;
import com.nightmare.applib.utils.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class CreateVirtualDisplay implements IHTTPHandler {
    public CreateVirtualDisplay() {
    }


    @Override
    public String route() {
        return "/createVirtualDisplay";
    }

    public static Map<Integer, VirtualDisplay> cache = new HashMap<>();

    Surface getVDSurface() {
        // Android 12 use the other way
        // because it will cause
        // java.lang.SecurityException: Given calling package android does not match caller's uid 2000
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return MediaCodec.createPersistentInputSurface();
        }
        SurfaceView surfaceView = new SurfaceView(FakeContext.get());
        return surfaceView.getHolder().getSurface();
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        L.d("createVirtualDisplayWithSurfaceView invoke");
        String useDeviceConfig = session.getParms().get("useDeviceConfig");
        String width, height, density;
        boolean useDeviceConfigBool = Boolean.parseBoolean(useDeviceConfig);
        if (useDeviceConfigBool) {
            WindowManager windowManager = (WindowManager) FakeContext.get().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
            width = displayMetrics.widthPixels + "";
            height = displayMetrics.heightPixels + "";
            density = displayMetrics.densityDpi + "";
        } else {
            width = session.getParms().get("width");
            height = session.getParms().get("height");
            density = session.getParms().get("density");
        }
        assert width != null;
        assert height != null;
        assert density != null;
        VirtualDisplay display = null;
        DisplayManager displayManager = null;
        try {
            // Android 11/12/13/14/15 (test on 2024.09.17) is ok
            //noinspection JavaReflectionMemberAccess
            displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        display = displayManager.createVirtualDisplay(
                "applib-vd",
                Integer.parseInt(width),
                Integer.parseInt(height),
                Integer.parseInt(density),
                getVDSurface(),
                getVirtualDisplayFlags()
        );
        cache.put(display.getDisplay().getDisplayId(), display);
        JSONObject json = null;
        try {
            json = DisplayUtil.getDisplayInfo(display.getDisplay());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "application/json",
                json.toString()
        );
    }

    private static int getVirtualDisplayFlags() {
        int VIRTUAL_DISPLAY_FLAG_PUBLIC = 1;
        int VIRTUAL_DISPLAY_FLAG_PRESENTATION = 1 << 1;
        int VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY = 1 << 3;
        int VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL = 1 << 8;
        int VIRTUAL_DISPLAY_FLAG_TRUSTED = 1 << 10;
        int VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP = 1 << 11;
        int VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED = 1 << 12;

        int flags = VIRTUAL_DISPLAY_FLAG_PUBLIC | VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL | VIRTUAL_DISPLAY_FLAG_PRESENTATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            flags |= VIRTUAL_DISPLAY_FLAG_TRUSTED | VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP | VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED;
        }
        return flags;
    }

    VirtualDisplay createVirtualDisplayWithSurfaceView(int width, int height, int densityDpi, Surface surface) {
        VirtualDisplay display = null;

        DisplayManager displayManager = null;
        try {
            displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        display = displayManager.createVirtualDisplay(
                "uncon-vd",
                width,
                height,
                densityDpi,
                surface,
                getVirtualDisplayFlags()
        );
//        cache.put(display.getDisplay().getDisplayId(), display);
        return display;
    }
}
