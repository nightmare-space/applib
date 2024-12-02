package com.nightmare.ass_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.GraphicBuffer;
import android.graphics.PixelFormat;
import android.hardware.HardwareBuffer;
import android.os.Build;
import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.L;
import com.nightmare.aas.ReflectUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import fi.iki.elonen.NanoHTTPD;

// 获取缩略图
public class Taskthumbnail extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "/taskthumbnail";
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

    public static <T> T unsafeCast(final Object obj) {
        //noinspection unchecked
        return (T) obj;
    }

    public static <T> T getHiddenField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return unsafeCast(field.get(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeHiddenMethod(Object obj, String methodName, Object... args) {
        try {
            Class<?>[] parameterTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Integer) {
                    parameterTypes[i] = int.class;
                } else if (args[i] instanceof Boolean) {
                    parameterTypes[i] = boolean.class;
                } else {
                    parameterTypes[i] = args[i].getClass();
                }
            }
            Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return unsafeCast(method.invoke(obj, args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String id = session.getParms().get("id");
        L.d("id -> " + id);
        byte[] bytes = null;
        try {
            long start = System.currentTimeMillis();
            IActivityTaskManager activityTaskManager = ActivityTaskManager.getService();
            ReflectUtil.listAllObject(activityTaskManager);
            Object snapshot = null;

            // Android 12/Android 15
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                L.d("S or VANILLA_ICE_CREAM");
                snapshot = invokeHiddenMethod(activityTaskManager, "getTaskSnapshot", Integer.parseInt(id), false);
                L.d("snapshot -> " + snapshot);
            }
            // Android 13/Android 14
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU || Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                L.d("TIRAMISU or UPSIDE_DOWN_CAKE");
                snapshot = invokeHiddenMethod(activityTaskManager, "getTaskSnapshot", Integer.parseInt(id), false, false);
                L.d("snapshot -> " + snapshot);
            }
            Object hardBuffer = getHiddenField(snapshot, "mSnapshot");
            L.d("hardBuffer -> " + hardBuffer);
            Object colorSpace = getHiddenField(snapshot, "mColorSpace");
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
