package com.nightmare.applib.handler;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Intent;
import android.content.pm.PackageManager;

import com.nightmare.applib.FakeContext;
import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.L;

import org.json.JSONException;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;

// 通过包名获取Main Activity
public class AppMainActivity extends IHTTPHandler {
    @Override
    public String route() {
        return "/app_main_activity";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        String mainActivity = getAppMainActivity(packageName);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mainActivity", mainActivity);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
    }

    /*
     * 通过包名获取Main Activity
     * */
    public String getAppMainActivity(String packageName) {
        StringBuilder builder = new StringBuilder();
        PackageManager pm = FakeContext.get().getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            builder.append(launchIntent.getComponent().getClassName());
        } else {
            L.d(packageName + "获取启动Activity失败");
        }
        return builder.toString();
//        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, null, 0, 0);
//        for (int i = 0; i < appList.size(); i++) {
//            ResolveInfo resolveInfo = appList.get(i);
//            String packageStr = resolveInfo.activityInfo.packageName;
//            if (packageStr.equals(packageName)) {
//                builder.append(resolveInfo.activityInfo.name).append("\n");
//                break;
//            }
//        }
//        return builder.toString();
    }


}
