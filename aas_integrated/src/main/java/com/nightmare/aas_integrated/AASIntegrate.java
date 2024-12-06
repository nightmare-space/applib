package com.nightmare.aas_integrated;

import android.content.Context;
import android.os.Looper;

import com.nightmare.aas.AndroidAPIServer;
import com.nightmare.aas_plugins.ActivityManagerPlugin;
import com.nightmare.aas_plugins.ChangeDisplayHandler;
import com.nightmare.aas_plugins.DisplayManagerPlugin;
import com.nightmare.aas_plugins.FilePlugin;
import com.nightmare.aas_plugins.PackageManagerPlugin;
import com.nightmare.aas_plugins.ActivityTaskManagerPlugin;

public class AASIntegrate {

    static public void main(String[] args) {
        AndroidAPIServer server = new AndroidAPIServer();
        server.startServerForShell(args);
        registerRoutes(server);
        Looper.loop();
    }

    public static int startServerFromActivity(Context context) {
        AndroidAPIServer server = new AndroidAPIServer();
        int port = server.startServerFromActivity(context);
        registerRoutes(server);
        return port;
    }

    private static void registerRoutes(AndroidAPIServer server) {
        server.registerPlugin(new PackageManagerPlugin());
        server.registerPlugin(new ChangeDisplayHandler());
        server.registerPlugin(new DisplayManagerPlugin());
        server.registerPlugin(new ActivityManagerPlugin());
        server.registerPlugin(new ActivityTaskManagerPlugin());
        server.registerPlugin(new FilePlugin());
    }

}
