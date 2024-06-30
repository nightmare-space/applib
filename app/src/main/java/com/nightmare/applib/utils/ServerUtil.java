package com.nightmare.applib.utils;

import com.nightmare.applib.AppServer;

import java.io.IOException;
import java.net.ServerSocket;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

public class ServerUtil {

    // 端口尝试的范围
    static final int RANGE_START = 14000;
    static final int RANGE_END = 14040;

    static final int SHELL_RANGE_START = 15000;
    static final int SHELL_RANGE_END = 15040;

    /**
     * 安全获得服务器的的方法
     */
    public static AppServer safeGetServer() {
        for (int i = RANGE_START; i < RANGE_END; i++) {
            AppServer server = new AppServer("0.0.0.0", i);
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                return server;
            } catch (IOException e) {
                L.d("端口" + i + "被占用");
            }
        }
        return null;
    }

    /**
     * 安全获得服务器的的方法
     */
    public static AppServer safeGetServerForADB() {
        for (int i = SHELL_RANGE_START; i < SHELL_RANGE_END; i++) {
            AppServer server = new AppServer("0.0.0.0", i);
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
                return server;
            } catch (IOException e) {
                L.d("端口" + i + "被占用");
            }
        }
        return null;
    }

    /**
     * 更安全的拿到一个ServerSocket
     * 有的时候会端口占用
     *
     * @return
     */
    public static ServerSocket safeGetServerSocket() {
        for (int i = RANGE_START; i < RANGE_END; i++) {
            try {
                return new ServerSocket(i);
            } catch (IOException e) {
                L.d("端口" + i + "被占用");
            }
        }
        return null;
    }

}
