package com.nightmare.aas_integrated;

import android.content.Context;
import android.os.Looper;

import com.nightmare.aas.AndroidAPIServer;
import com.nightmare.aas.L;
import com.nightmare.aas_plugins.AppActivityHandler;
import com.nightmare.aas_plugins.AppDetailHandler;
import com.nightmare.aas_plugins.AppInfosHandler;
import com.nightmare.aas_plugins.AppMainActivity;
import com.nightmare.aas_plugins.AppPermissionHandler;
import com.nightmare.aas_plugins.CMDHandler;
import com.nightmare.aas_plugins.ChangeDisplayHandler;
import com.nightmare.aas_plugins.DisplayHandler;
import com.nightmare.aas_plugins.FileHandler;
import com.nightmare.aas_plugins.IconHandler;
import com.nightmare.aas_plugins.OpenAppHandler;
import com.nightmare.aas_plugins.Resizevd;
import com.nightmare.aas_plugins.StopActivityHandler;
import com.nightmare.aas_plugins.TaskHandler;
import com.nightmare.aas_plugins.Taskthumbnail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class AASIntegrate extends AndroidAPIServer {
    public AASIntegrate() {
    }


    public static void main(String[] args) {
//        L.d("AASIntegrate");
        if (Objects.equals(args[0], "sula")) {
            // TODO: 这个多在几个安卓模拟器上测试一下
            L.serverLogPath = "/storage/emulated/0/Android/data/com.nightmare.sula" + "/app_server_log";
            portDirectory = "/storage/emulated/0/Android/data/com.nightmare.sula";
            L.d("Dex Server for Sula");
        }
        L.d("Welcome!!!");
        L.d("args -> " + Arrays.toString(args));
        AASIntegrate server = new AASIntegrate();
        server.startServerForShell();
        server.registerRoutes();
        Looper.loop();
    }

    @Override
    public int startServerFromActivity(Context context) {
        String dataPath = context.getFilesDir().getPath();
        L.serverLogPath = dataPath + "/app_server_log";
        portDirectory = dataPath;
        int port = super.startServerFromActivity(context);
        registerRoutes();
        return port;
    }

    void registerRoutes() {
        registerPlugin(new AppActivityHandler());
        registerPlugin(new AppDetailHandler());
        registerPlugin(new AppInfosHandler());
//        addHandler(new AppInfosHandlerV1());
        registerPlugin(new AppMainActivity());
        registerPlugin(new AppPermissionHandler());
        registerPlugin(new ChangeDisplayHandler());
        registerPlugin(new CMDHandler());
        registerPlugin(new DisplayHandler());
        registerPlugin(new IconHandler());
        registerPlugin(new FileHandler());
        registerPlugin(new OpenAppHandler());
        registerPlugin(new Resizevd());
        registerPlugin(new StopActivityHandler());
        registerPlugin(new TaskHandler());
        registerPlugin(new Taskthumbnail());
    }

}
