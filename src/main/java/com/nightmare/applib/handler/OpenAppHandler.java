package com.nightmare.applib.handler;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.L;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import fi.iki.elonen.NanoHTTPD;
public class OpenAppHandler extends IHTTPHandler {
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
            jsonObject.put("status", "success");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
    }


    public void startActivity(String packageName, String activity, String displayId) {
        // 这里要支持有真实 Activity Context 的情况
        if (true) {
            String cmd = "am start --display " + displayId + " -n " + packageName + "/" + activity;
            L.d("start activity cmd : " + cmd);
            // adb -s $serial shell am start -n $packageName/$activity
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
            return;
        }
//        try {
//            Intent intent = new Intent();
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            // 取消activity动画
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK);
//            ActivityOptions options = null;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                options = ActivityOptions.makeBasic().setLaunchDisplayId(Integer.parseInt(displayId));
//
//                ReflectUtil.listAllObject(options);
////                options.setLaunchWindowingMode();
//
//            }
//            ComponentName cName = new ComponentName(packageName, activity);
//            intent.setComponent(cName);
////            context.startActivity(intent);
//            @SuppressLint({"NewApi", "LocalSuppress"})
//            Bundle bundle = options.toBundle();
//            bundle.putInt("android.activity.activityType", 2);
//            context.startActivity(intent, options.toBundle());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}
