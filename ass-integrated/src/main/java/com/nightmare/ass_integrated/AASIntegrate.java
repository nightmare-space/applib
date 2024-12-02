package com.nightmare.ass_integrated;

import com.nightmare.aas.AndroidAPIServer;
import com.nightmare.ass_plugins.AppActivityHandler;
import com.nightmare.ass_plugins.AppDetailHandler;
import com.nightmare.ass_plugins.AppInfosHandler;
import com.nightmare.ass_plugins.AppMainActivity;
import com.nightmare.ass_plugins.AppPermissionHandler;
import com.nightmare.ass_plugins.CMDHandler;
import com.nightmare.ass_plugins.ChangeDisplayHandler;
import com.nightmare.ass_plugins.DisplayHandler;
import com.nightmare.ass_plugins.FileHandler;
import com.nightmare.ass_plugins.IconHandler;
import com.nightmare.ass_plugins.OpenAppHandler;
import com.nightmare.ass_plugins.Resizevd;
import com.nightmare.ass_plugins.StopActivityHandler;
import com.nightmare.ass_plugins.TaskHandler;
import com.nightmare.ass_plugins.Taskthumbnail;

public class AASIntegrate extends AndroidAPIServer {
    public AASIntegrate(String address, int port) {
        super(address, port);
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
