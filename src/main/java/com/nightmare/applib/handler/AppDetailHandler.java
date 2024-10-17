package com.nightmare.applib.handler;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import android.content.pm.PackageInfo;
import com.nightmare.applib.interfaces.IHTTPHandler;
import org.json.JSONException;
import org.json.JSONObject;
import fi.iki.elonen.NanoHTTPD;

// 获取单个App的详细信息
public class AppDetailHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/appdetail";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        String detail = getAppDetail(packageName);
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", detail);

    }

    public String getAppDetail(String data) {
        JSONObject jsonObject = new JSONObject();
        try {
            PackageInfo packageInfo = IconHandler.getPackageInfo(data);
            jsonObject.put("firstInstallTime", packageInfo.firstInstallTime);
            jsonObject.put("lastUpdateTime", packageInfo.lastUpdateTime);
            jsonObject.put("dataDir", packageInfo.applicationInfo.dataDir);
            jsonObject.put("nativeLibraryDir", packageInfo.applicationInfo.nativeLibraryDir);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }
}
