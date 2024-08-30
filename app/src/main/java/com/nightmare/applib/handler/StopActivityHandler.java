package com.nightmare.applib.handler;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.L;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class StopActivityHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "activity_stop";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) { String packageName = session.getParms().get("package");
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
