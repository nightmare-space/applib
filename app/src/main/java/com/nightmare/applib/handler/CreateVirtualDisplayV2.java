package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.view.SurfaceView;

import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.wrappers.DisplayManagerV2;

import fi.iki.elonen.NanoHTTPD;

public class CreateVirtualDisplayV2 implements IHTTPHandler {
    @Override
    public String route() {
        return "/CreateVirtualDisplayV2";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        DisplayManagerV2 displayManagerV2 = DisplayManagerV2.create();
        SurfaceView surfaceView = new SurfaceView(appChannel.context);
        try {
            displayManagerV2.createVirtualDisplay("test", 1080, 1920, 240, surfaceView.getHolder().getSurface());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "");
    }
}
