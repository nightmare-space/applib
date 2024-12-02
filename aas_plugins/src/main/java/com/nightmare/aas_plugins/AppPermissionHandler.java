package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;

import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.ContextStore;
import com.nightmare.aas.FakeContext;
import com.nightmare.aas.L;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;

public class AppPermissionHandler extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "/app_permission";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        String permissions = getAppPermissions(packageName);
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", permissions);
    }


    public String getAppPermissions(String packageName) {
        PackageManager packageManager = ContextStore.getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] permissions = packageInfo.requestedPermissions;
//            L.d("permissions: " + permissions);
            if (permissions == null) {
                return "[]";
            }
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonObject.put("datas", jsonArray);
            for (String permission : permissions) {
                JSONObject object = new JSONObject();
                object.put("name", permission);
                try {
                    PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);
//                    L.d("permissionInfo: " + permissionInfo);
                    if (permissionInfo != null) {
                        CharSequence description = permissionInfo.loadDescription(packageManager);
                        if (description != null) {
                            object.put("description", description.toString());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                jsonArray.put(object);
            }
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }
}
