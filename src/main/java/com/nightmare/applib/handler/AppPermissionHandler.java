package com.nightmare.applib.handler;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import com.nightmare.applib.FakeContext;
import com.nightmare.applib.interfaces.IHTTPHandler;
import fi.iki.elonen.NanoHTTPD;

public class AppPermissionHandler extends IHTTPHandler {
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


    public String getAppPermissions(String data) {
        StringBuilder builder = new StringBuilder();

        PackageManager pm = FakeContext.get().getPackageManager();
        try {
            PackageInfo packageInfo = IconHandler.getPackageInfo(data, PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
            String[] usesPermissionsArray = packageInfo.requestedPermissions;
            for (String usesPermissionName : usesPermissionsArray) {

                //得到每个权限的名字,如:android.permission.INTERNET
//                    print("usesPermissionName=" + usesPermissionName);
                builder.append(usesPermissionName);
                //通过usesPermissionName获取该权限的详细信息
                PermissionInfo permissionInfo = pm.getPermissionInfo(usesPermissionName, 0);

                //获得该权限属于哪个权限组,如:网络通信
//                PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
//                System.out.println("permissionGroup=" + permissionGroupInfo.loadLabel(packageManager).toString());

                //获取该权限的标签信息,比如:完全的网络访问权限
                String permissionLabel = AppInfosHandler.getLabel(packageInfo.applicationInfo);
//                    print("permissionLabel=" + permissionLabel);
                //获取该权限的详细描述信息,比如:允许该应用创建网络套接字和使用自定义网络协议
                //浏览器和其他某些应用提供了向互联网发送数据的途径,因此应用无需该权限即可向互联网发送数据.
                String permissionDescription = permissionInfo.loadDescription(pm).toString();
                builder.append(" ").append(permissionDescription);
                boolean isHasPermission = PackageManager.PERMISSION_GRANTED == pm.checkPermission(permissionInfo.name, data);
                builder.append(" ").append(isHasPermission).append("\r");
//                    print("permissionDescription=" + permissionDescription);
//                    print("===========================================");
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return builder.toString();
    }
}
