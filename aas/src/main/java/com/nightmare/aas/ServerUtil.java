package com.nightmare.aas;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class ServerUtil {

    // 端口尝试的范围
    static final int RANGE_START = 14000;
    static final int RANGE_END = 14040;

    static final int SHELL_RANGE_START = 15000;
    static final int SHELL_RANGE_END = 15040;

    /**
     * 安全获得服务器的的方法
     * safe start app server for activity
     */
    public static AndroidAPIServerHTTPD safeGetServerForActivity() {
        return safeGetServer(RANGE_START, RANGE_END);
    }

    /**
     * 安全获得服务器的的方法
     * safe start app server for shell
     */
    public static AndroidAPIServerHTTPD safeGetServerForShell() {
        return safeGetServer(SHELL_RANGE_START, SHELL_RANGE_END);
    }


    public static AndroidAPIServerHTTPD safeGetServer(int start, int end) {
        for (int i = start; i < end; i++) {
            AndroidAPIServerHTTPD server = new AndroidAPIServerHTTPD(i);
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                return server;
            } catch (IOException e) {
                L.d("端口" + i + "被占用");
            }
        }
        return null;
    }

}
