package com.nightmare.ass_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.hardware.display.VirtualDisplay;
import android.os.Build;
import com.nightmare.aas.AndroidAPIPlugin;

import fi.iki.elonen.NanoHTTPD;

// 改变虚拟显示器尺寸
public class Resizevd extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "virtual_display_resize";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String id = session.getParms().get("id");
        String width = session.getParms().get("width");
        String height = session.getParms().get("height");
        String density = session.getParms().get("density");
        VirtualDisplay display = DisplayHandler.cache.get(Integer.parseInt(id));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            display.resize(Integer.parseInt(width), Integer.parseInt(height), Integer.parseInt(density));
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                display.getDisplay().getDisplayId() + "");
    }
}
