package com.nightmare.applib.handler;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.nightmare.applib.FakeContext;
import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.DisplayUtil;
import com.nightmare.applib.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fi.iki.elonen.NanoHTTPD;

public class DisplayHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/display";
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

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String action = session.getParms().get("action");
        assert action != null;
        if (action.equals("getDisplays")) {
            // Android 11/12/13/14/15 (test on 2024.09.17) is ok
            DisplayManager displayManager = null;
            try {
                //noinspection JavaReflectionMemberAccess
                displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            Display[] displayss = displayManager.getDisplays();
            L.d("DisplaysHandler Invoke");
            JSONObject jsonObjectResult = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (Display display : displayss) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = DisplayUtil.getDisplayInfo(display);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                jsonArray.put(jsonObject);
            }
            try {
                jsonObjectResult.put("datas", jsonArray);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObjectResult.toString());
        }
        if (action.equals("createVirtualDisplay")) {
            L.d("createVirtualDisplayWithSurfaceView invoke");
            Map<String, String> params = session.getParms();
            String useDeviceConfig = params.get("useDeviceConfig");
            String displayName = params.get("displayName");
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
                width = params.get("width");
                height = params.get("height");
                density = params.get("density");
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
            if (displayName == null) {
                displayName = "applib-vd";
            }
            display = displayManager.createVirtualDisplay(
                    displayName,
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
        String displayId = session.getParms().get("id");
        assert displayId != null;
        if(cache.containsKey(Integer.parseInt(displayId))) {
            Objects.requireNonNull(cache.get(Integer.parseInt(displayId))).release();
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "success");
    }
}
