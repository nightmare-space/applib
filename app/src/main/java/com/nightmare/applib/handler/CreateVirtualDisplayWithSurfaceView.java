package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Context;
import android.hardware.display.VirtualDisplay;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.DisplayUtil;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.wrappers.ServiceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

import fi.iki.elonen.NanoHTTPD;

public class CreateVirtualDisplayWithSurfaceView implements IHTTPHandler {
    @Override
    public String route() {
        return "/createVirtualDisplayWithSurfaceView";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        L.d("createVirtualDisplayWithSurfaceView invoke");
        String useDeviceConfig = session.getParms().get("useDeviceConfig");
        SurfaceView surfaceView = new SurfaceView(appChannel.context);
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
        assert width != null;
        assert height != null;
        assert density != null;
        VirtualDisplay display = null;
        try {
            display = ServiceManager.getDisplayManager().createVirtualDisplay(
                    surfaceView.getHolder().getSurface(),
                    Integer.parseInt(width),
                    Integer.parseInt(height),
                    Integer.parseInt(density)
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
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
