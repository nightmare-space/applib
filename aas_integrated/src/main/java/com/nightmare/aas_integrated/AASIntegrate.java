package com.nightmare.aas_integrated;

import android.content.Context;
import android.os.Looper;

import com.nightmare.aas.AndroidAPIServer;
import com.nightmare.aas.L;
import com.nightmare.aas_plugins.AMPlugin;
import com.nightmare.aas_plugins.CMDHandler;
import com.nightmare.aas_plugins.ChangeDisplayHandler;
import com.nightmare.aas_plugins.DMPlugin;
import com.nightmare.aas_plugins.FilePlugin;
import com.nightmare.aas_plugins.PMPlugin;
import com.nightmare.aas_plugins.ATMPlugin;

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
//        addHandler(new AppInfosHandlerV1());
        registerPlugin(new PMPlugin());
        registerPlugin(new ChangeDisplayHandler());
        registerPlugin(new CMDHandler());
        registerPlugin(new DMPlugin());
        registerPlugin(new FilePlugin());
        registerPlugin(new AMPlugin());
        registerPlugin(new ATMPlugin());
    }

}
