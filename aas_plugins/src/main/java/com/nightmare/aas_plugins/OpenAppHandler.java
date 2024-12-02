package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.FakeContext;
import com.nightmare.aas.L;
import com.nightmare.aas.ShizukuSystemServerApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class OpenAppHandler extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "/start_activity";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        // 要保证参数存在，不然服务可能会崩
        String packageName = session.getParms().get("package");
        String activity = session.getParms().get("activity");
        String id = session.getParms().get("displayId");
        startActivity(packageName, activity, id);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("result", "success");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
    }


    public void startActivity(String packageName, String activity, String displayId) {
        Intent launchIntent = new Intent();
        launchIntent.setClassName(packageName, activity);

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle options = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityOptions launchOptions = ActivityOptions.makeBasic();
            launchOptions.setLaunchDisplayId(Integer.parseInt(displayId));
            options = launchOptions.toBundle();
        }
        try {
            IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
            IActivityManager activityManagerServices = IActivityManager.Stub.asInterface(binder);
            activityManagerServices.startActivityAsUser(
                    /* caller */ null,
                    /* callingPackage */ FakeContext.PACKAGE_NAME,
                    /* intent */ launchIntent,
                    /* resolvedType */ null,
                    /* resultTo */ null,
                    /* resultWho */ null,
                    /* requestCode */ 0,
                    /* startFlags */ 0,
                    /* profilerInfo */ null,
                    /* bOptions */ options,
                    /* userId */ /* UserHandle.USER_CURRENT */ -2
            );
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

}
