package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.app.IActivityManager;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.L;

import org.json.JSONException;
import org.json.JSONObject;

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
        IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
        IActivityManager activityManagerServices = IActivityManager.Stub.asInterface(binder);
        JSONObject jsonObject = new JSONObject();
        try {
            activityManagerServices.forceStopPackage(packageName, -2);
            jsonObject.put("result", "success");
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
        } catch (RemoteException | JSONException e) {
            try {
                jsonObject.put("result", "success");
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
        }
    }
}
