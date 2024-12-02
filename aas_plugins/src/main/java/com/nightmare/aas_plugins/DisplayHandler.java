package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.ContextStore;
import com.nightmare.aas.FakeContext;
import com.nightmare.aas.L;
import com.nightmare.aas.ReflectUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fi.iki.elonen.NanoHTTPD;

public class DisplayHandler extends AndroidAPIPlugin {
    public DisplayHandler() {
//        testChangeRefreshRate();
    }

    int VANILLA_ICE_CREAM = 35;

    @Override
    public String route() {
        return "/display";
    }

    public static Map<Integer, VirtualDisplay> cache = new HashMap<>();

    Surface getVDSurface() {
//        SurfaceTexture surfaceTexture = new SurfaceTexture(0);
//        ;
//        Surface surface = new Surface(surfaceTexture);
//        return surface;
        // Android 12 use the other way
        // because it will cause
        // java.lang.SecurityException: Given calling package android does not match caller's uid 2000
        // android 15
        L.d(" Build.VERSION.SDK_INT -> " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == VANILLA_ICE_CREAM) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return MediaCodec.createPersistentInputSurface();
            }
        }
        SurfaceView surfaceView = new SurfaceView(FakeContext.get());
        return surfaceView.getHolder().getSurface();
    }

    Surface getVDSurfaceNew(int width, int height) {
        MediaCodec mediaCodec = null;
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 120);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 800_0000);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 0);
            format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 100_1000);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = mediaCodec.createInputSurface();
            return surface;
        } catch (IOException e) {

        }
        return null;
    }

    private static final int VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH = 1 << 6;
    private static final int VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT = 1 << 7;
    private static final int VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL = 1 << 8;
    private static final int VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS = 1 << 9;
    private static final int VIRTUAL_DISPLAY_FLAG_TRUSTED = 1 << 10;
    private static final int VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP = 1 << 11;
    private static final int VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED = 1 << 12;
    private static final int VIRTUAL_DISPLAY_FLAG_TOUCH_FEEDBACK_DISABLED = 1 << 13;
    private static final int VIRTUAL_DISPLAY_FLAG_OWN_FOCUS = 1 << 14;
    private static final int VIRTUAL_DISPLAY_FLAG_DEVICE_DISPLAY_GROUP = 1 << 15;
    private static final int VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY = 1 << 3;
    private static final int VIRTUAL_DISPLAY_FLAG_PRESENTATION = 1 << 1;
    private static final int VIRTUAL_DISPLAY_FLAG_PUBLIC = 1;

    private static int getVirtualDisplayFlags() {
        int flagsa = VIRTUAL_DISPLAY_FLAG_PUBLIC
                | VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                | VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH
                | VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT
                | VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL
                | VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS
                | VIRTUAL_DISPLAY_FLAG_TRUSTED
                | VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP
                | VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED
                | VIRTUAL_DISPLAY_FLAG_TOUCH_FEEDBACK_DISABLED
                | VIRTUAL_DISPLAY_FLAG_OWN_FOCUS
                | VIRTUAL_DISPLAY_FLAG_DEVICE_DISPLAY_GROUP;
        int flags = VIRTUAL_DISPLAY_FLAG_PUBLIC
                | VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                | VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH
                | VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT
                // Check 这个 flag 移除后，关闭虚拟显示器，app会不会退出
//                | VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL
                // 这行能让魅族直接把 Launcher 启动到这个虚拟显示器上
//                | VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS
                | VIRTUAL_DISPLAY_FLAG_TOUCH_FEEDBACK_DISABLED
                | VIRTUAL_DISPLAY_FLAG_OWN_FOCUS
                | VIRTUAL_DISPLAY_FLAG_DEVICE_DISPLAY_GROUP;
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
            if (ContextStore.getContext() instanceof FakeContext) {
                try {
                    //noinspection JavaReflectionMemberAccess
                    displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(ContextStore.getContext());
//                ReflectUtil.listAllObject(displayManager);
                } catch (IllegalAccessException | InstantiationException |
                         InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            } else {
                displayManager = (DisplayManager) ContextStore.getContext().getSystemService(Context.DISPLAY_SERVICE);
            }
            Display[] displays = displayManager.getDisplays();
            L.d("DisplaysHandler Invoke");
            JSONObject jsonObjectResult = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (Display display : displays) {
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
                displayName = "Virtual Display";
            }
            Surface surface = getVDSurface();
            display = displayManager.createVirtualDisplay(
                    displayName,
                    Integer.parseInt(width),
                    Integer.parseInt(height),
                    Integer.parseInt(density),
                    surface,
                    getVirtualDisplayFlags()
            );
//            try {
//                display = ServiceManager.getDisplayManager().createVirtualDisplay(
//                        displayName,
//                        Integer.parseInt(width),
//                        Integer.parseInt(height),
//                        Integer.parseInt(density),
//                        surface,
//                        getVirtualDisplayFlags()
//                );
//            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException |
//                     InstantiationException | InvocationTargetException e) {
//                throw new RuntimeException(e);
//            }
            assert display != null;
            cache.put(display.getDisplay().getDisplayId(), display);
            JSONObject json = null;
            try {
                json = DisplayUtil.getDisplayInfo(display.getDisplay());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            Display display1 = (Display) ReflectUtil.invokeMethod(displayManager, "getDisplay", display.getDisplay().getDisplayId());
            L.d("display1 -> " + display1);
//
//            IBinder displayToken = DisplayControl.getPhysicalDisplayToken(display.getDisplay().getDisplayId());
//            L.d("displayToken -> " + displayToken);
//
//            Object token = ReflectUtil.invokeMethod(display, "getToken");
//            L.d("token -> " + token);

            return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    "application/json",
                    json.toString()
            );
        }
        String displayId = session.getParms().get("id");
        assert displayId != null;
        if (cache.containsKey(Integer.parseInt(displayId))) {
            Objects.requireNonNull(cache.get(Integer.parseInt(displayId))).release();
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "success");
    }

    void testChangeRefreshRate() {

        long[] ids = DisplayControl.getPhysicalDisplayIds();
        for (long id : ids) {
            L.d("");
            L.d("");
            L.d("");
            L.d("");
            L.d("Display Id -> " + id);
            IBinder displayToken = DisplayControl.getPhysicalDisplayToken(id);

            try {
//                    ReflectUtil.listAllObject(SurfaceControl.class);

                SurfaceControl.setBootDisplayMode(displayToken, 0);
                Object o = SurfaceControl.getDynamicDisplayInfo(id);
                L.d("displayToken-> " + displayToken);
                Field supportedDisplayModesField = o.getClass().getField("supportedDisplayModes");
                Field activeDisplayModeIdField = o.getClass().getField("activeDisplayModeId");
                Field renderFrameRateField = o.getClass().getField("renderFrameRate");
                Field supportedColorModesField = o.getClass().getField("supportedColorModes");
                Field activeColorModeField = o.getClass().getField("activeColorMode");
                Field hdrCapabilitiesField = o.getClass().getField("hdrCapabilities");
                Field autoLowLatencyModeSupportedField = o.getClass().getField("autoLowLatencyModeSupported");
                Field gameContentTypeSupportedField = o.getClass().getField("gameContentTypeSupported");
                Field preferredBootDisplayModeField = o.getClass().getField("preferredBootDisplayMode");
                Object supportedDisplayModes = supportedDisplayModesField.get(o);
                Object activeDisplayModeId = activeDisplayModeIdField.get(o);
                Object renderFrameRate = renderFrameRateField.get(o);
                int[] supportedColorModes = (int[]) supportedColorModesField.get(o);
                Object activeColorMode = activeColorModeField.get(o);
                Object hdrCapabilities = hdrCapabilitiesField.get(o);
                Object autoLowLatencyModeSupported = autoLowLatencyModeSupportedField.get(o);
                Object gameContentTypeSupported = gameContentTypeSupportedField.get(o);
                Object preferredBootDisplayMode = preferredBootDisplayModeField.get(o);
                for (Object object : (Object[]) supportedDisplayModes) {
                    L.d("mode -> " + object);
                }
                L.d("activeDisplayModeId -> " + activeDisplayModeId);
                L.d("renderFrameRate -> " + renderFrameRate);
                L.d("supportedColorModes -> " + Arrays.toString(supportedColorModes));
//                for (Object object : (Object[]) supportedColorModes) {
//                    L.d("supportedColorMode -> " + object);
//                }
                L.d("activeColorMode -> " + activeColorMode);
                L.d("hdrCapabilities -> " + hdrCapabilities);
                L.d("autoLowLatencyModeSupported -> " + autoLowLatencyModeSupported);
                L.d("gameContentTypeSupported -> " + gameContentTypeSupported);
                L.d("preferredBootDisplayMode -> " + preferredBootDisplayMode);


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            L.d("Display Token -> " + displayToken);

        }
    }
}
