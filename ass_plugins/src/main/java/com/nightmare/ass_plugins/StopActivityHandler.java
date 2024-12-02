package com.nightmare.ass_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.L;

import java.io.IOException;
import fi.iki.elonen.NanoHTTPD;

public class StopActivityHandler extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "/stop_activity";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        String cmd = "am force-stop " + packageName;
        L.d("stopActivity activity cmd : " + cmd);
        // adb -s $serial shell am start -n $packageName/$activity
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "success");
    }
}
