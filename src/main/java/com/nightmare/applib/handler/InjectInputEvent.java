package com.nightmare.applib.handler;

import android.view.MotionEvent;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import com.nightmare.applib.InputDispatcher;
import com.nightmare.applib.Position;
import com.nightmare.applib.interfaces.IHTTPHandler;

import fi.iki.elonen.NanoHTTPD;

public class InjectInputEvent implements IHTTPHandler {
    @Override
    public String route() {
        return "/injectInputEvent";
    }

    public static InputDispatcher inputDispatcher = new InputDispatcher();

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String action = session.getParms().get("action");
        String pointerId = session.getParms().get("pointerId");
        String deviceWidth = session.getParms().get("width");
        String deviceHeight = session.getParms().get("height");
        String x = session.getParms().get("x");
        String y = session.getParms().get("y");
        String displayId = session.getParms().get("displayId");
        String actionButton = session.getParms().get("actionButton");
        String buttons = session.getParms().get("buttons");
        int displayIdInt = Integer.parseInt(displayId);
        int actionInt = Integer.parseInt(action);
        long pointerIdInt = Long.parseLong(pointerId);
        int xInt = Integer.parseInt(x);
        int yInt = Integer.parseInt(y);
        int widthInt = Integer.parseInt(deviceWidth);
        int heightInt = Integer.parseInt(deviceHeight);
        int actionButtonInt = Integer.parseInt(actionButton);
        int buttonsInt = Integer.parseInt(buttons);
        Position position = new Position(xInt, yInt, widthInt, heightInt);
        inputDispatcher.setDisplayId(displayIdInt);
        float pressure = 0f;
        if (actionInt == MotionEvent.ACTION_DOWN || actionInt == MotionEvent.ACTION_MOVE) {
            pressure = 1f;
        }
        boolean success = inputDispatcher.injectTouch(actionInt, pointerIdInt, position, pressure,
                actionButtonInt, buttonsInt);
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/text", "success:" + success);
    }
}
