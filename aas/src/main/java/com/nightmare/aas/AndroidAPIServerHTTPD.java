package com.nightmare.aas;

import android.util.Log;

import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class AndroidAPIServerHTTPD extends NanoHTTPD {
    public AndroidAPIServerHTTPD(int port) {
        super(port);
    }


    List<AndroidAPIPlugin> plugins;

    AndroidAPIServer aas;

    void setAndroidAPIServer(AndroidAPIServer aas) {
        this.aas = aas;
        plugins = aas.plugins;
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String url = session.getUri();
            Log.d("AndroidAPIServerHTTPD", "url -> " + url);
            if (url.startsWith("/check")) {
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "ok");
            }
            for (AndroidAPIPlugin plugin : plugins) {
                if (!plugin.route().isEmpty() && url.startsWith(plugin.route())) {
                    return plugin.handle(session);
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "route not found");
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
        }
    }
}
