package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.GraphicBuffer;
import android.graphics.PixelFormat;
import android.hardware.HardwareBuffer;
import android.os.Build;

import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.helper.L;
import com.nightmare.aas.helper.ReflectionHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import fi.iki.elonen.NanoHTTPD;

/**
 * ActivityTaskManager Plugin
 */
public class ActivityTaskManagerPlugin extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "/task_thumbnail";
    }

    public Bitmap graphicBufferToBitmap(GraphicBuffer graphicBuffer) {
        int width = graphicBuffer.getWidth();
        int height = graphicBuffer.getHeight();
        int format = graphicBuffer.getFormat();

        Bitmap.Config config;
        if (format == PixelFormat.RGBA_8888) {
            config = Bitmap.Config.ARGB_8888;
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        graphicBuffer.lockCanvas().drawBitmap(bitmap, 0, 0, null);
        graphicBuffer.unlockCanvasAndPost(graphicBuffer.lockCanvas());

        return bitmap;
    }


    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String id = session.getParms().get("id");
        L.d("id -> " + id);
        byte[] bytes = null;
        try {
            long start = System.currentTimeMillis();
            IActivityTaskManager activityTaskManager = ActivityTaskManager.getService();
            ReflectionHelper.listAllObject(activityTaskManager);
            Object snapshot = null;

            // Android 12/Android 15
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == 35) {
                L.d("S or VANILLA_ICE_CREAM");
                snapshot = ReflectionHelper.invokeHiddenMethod(activityTaskManager, "getTaskSnapshot", Integer.parseInt(id), false);
                L.d("snapshot -> " + snapshot);
            }
            // Android 13/Android 14
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU || Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                L.d("TIRAMISU or UPSIDE_DOWN_CAKE");
                snapshot = ReflectionHelper.invokeHiddenMethod(activityTaskManager, "getTaskSnapshot", Integer.parseInt(id), false, false);
                L.d("snapshot -> " + snapshot);
            }
            Object hardBuffer = ReflectionHelper.getHiddenField(snapshot, "mSnapshot");
            L.d("hardBuffer -> " + hardBuffer);
            Object colorSpace = ReflectionHelper.getHiddenField(snapshot, "mColorSpace");
            //
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                HardwareBuffer hardwareBuffer = (HardwareBuffer) hardBuffer;
                Bitmap bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, (ColorSpace) colorSpace);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                bytes = baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "image/png", new ByteArrayInputStream(bytes), bytes.length);
    }
}
