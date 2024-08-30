package com.nightmare.applib.handler;

import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.DisplayUtil;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.wrappers.ServiceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

// 创建虚拟显示器
public class CreateVirtualDisplay implements IHTTPHandler {
    @Override
    public String route() {
        return "create_virtual_display";
    }

    public static final int IFRAME_INTERVAL = 0;
    // MediaFormat需要的，比特率
    public static final int BIT_RATE = 800_0000;
    // MediaFormat需要的
    public static final int REPEAT_FRAME_DELAY_US = 100_000;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static MediaFormat createFormat(String videoMimeType) {
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, videoMimeType);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        // must be present to configure the encoder, but does not impact the actual frame rate, which is variable
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            format.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_LIMITED);
        }
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        // display the very first frame, and recover from bad quality when no new frames
        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, REPEAT_FRAME_DELAY_US); // µs
//        if (maxFps > 0) {
//            // The key existed privately before Android 10:
//            // <https://android.googlesource.com/platform/frameworks/base/+/625f0aad9f7a259b6881006ad8710adce57d1384%5E%21/>
//            // <https://github.com/Genymobile/scrcpy/issues/488#issuecomment-567321437>
//            format.setFloat(KEY_MAX_FPS_TO_ENCODER, maxFps);
//        }

        return format;
    }


    public static Map<Integer, VirtualDisplay> cache = new HashMap<>();

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session)  { String useDeviceConfig = session.getParms().get("useDeviceConfig");
        String width, height, density;
        boolean useDeviceConfigBool = Boolean.parseBoolean(useDeviceConfig);
        if (useDeviceConfigBool) {
            WindowManager windowManager = (WindowManager) appChannel.context.getSystemService(Context.WINDOW_SERVICE);
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
        L.d("width -> " + width + " height -> " + height + " density -> " + density);
        MediaFormat format = createFormat(MIMETYPE_VIDEO_AVC);
        format.setInteger(MediaFormat.KEY_WIDTH, Integer.parseInt(width));
        format.setInteger(MediaFormat.KEY_HEIGHT, Integer.parseInt(height));
        MediaCodec codec = null;
        try {
            codec = MediaCodec.createEncoderByType(MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        codec.start();
//                SurfaceView surfaceView = new SurfaceView(appChannel.context);
//                Surface surface = surfaceView.getHolder().getSurface();
        Surface surface = codec.createInputSurface();
        VirtualDisplay display = null;
        try {
            display = ServiceManager.getDisplayManager().createVirtualDisplay(
                    surface,
                    Integer.parseInt(width),
                    Integer.parseInt(height),
                    Integer.parseInt(density)
            );
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException |
                 InstantiationException | NoSuchFieldException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assert display != null;
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
}
