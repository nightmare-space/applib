package com.nightmare.ass_plugins;
import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.L;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class TaskHandler extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "/tasks";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
//        return newFixedLengthResponse(
//                    NanoHTTPD.Response.Status.OK,
//                    "application/json",
//                    ""
//            );
        L.d("TaskHandler handle");
        try {
            return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    "application/json",
                    TaskUtil.getRecentTasksJson().toString()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
