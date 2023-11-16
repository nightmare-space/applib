package com.nightmare.applib;

import android.os.Build;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

import com.nightmare.applib.utils.L;
import com.nightmare.applib.wrappers.InputManagerSimulate;
import com.nightmare.applib.wrappers.ServiceManager;

public class InputDispatcher {
    public InputDispatcher() {
        initPointers();
    }

    private long lastTouchDown;
    private final PointersState pointersState = new PointersState();
    private final MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[PointersState.MAX_POINTERS];
    private final MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[PointersState.MAX_POINTERS];
    // control_msg.h values of the pointerId field in inject_touch_event message
    private static final int POINTER_ID_MOUSE = -1;
    private static final int POINTER_ID_VIRTUAL_MOUSE = -3;
    private static final int DEFAULT_DEVICE_ID = 0;

    private int displayId = 0;
    public static final int INJECT_MODE_ASYNC = InputManagerSimulate.INJECT_INPUT_EVENT_MODE_ASYNC;

    private void initPointers() {
        for (int i = 0; i < PointersState.MAX_POINTERS; ++i) {
            MotionEvent.PointerProperties props = new MotionEvent.PointerProperties();
            props.toolType = MotionEvent.TOOL_TYPE_FINGER;

            MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
            coords.orientation = 0;
            coords.size = 0;

            pointerProperties[i] = props;
            pointerCoords[i] = coords;
        }
    }

    public void setDisplayId(int id) {
        // L.d("setDisplayId: " + id);
        displayId = id;
    }

    public synchronized boolean injectTouch(int action, long pointerId, Position position, float pressure, int actionButton, int buttons) {
        L.d("injectTouch start invoked pointerId=" + pointerId + " action=" + action + " position=" + position + " pressure=" + pressure + " actionButton=" + actionButton + " buttons=" + buttons + "");
        long now = SystemClock.uptimeMillis();

        Point point = position.getPoint();
        if (point == null) {
            L.d("Ignore touch event, it was generated for a different device size");
            return false;
        }

        int pointerIndex = pointersState.getPointerIndex(pointerId);
        if (pointerIndex == -1) {
            L.d("Too many pointers for touch event pointerId=" + pointerId);
            return false;
        }
        Pointer pointer = pointersState.get(pointerIndex);
        pointer.setPoint(point);
        pointer.setPressure(pressure);

        int source;
        if (pointerId == POINTER_ID_MOUSE || pointerId == POINTER_ID_VIRTUAL_MOUSE) {
            L.d("mouse event (pointerId=" + pointerId + ")");
            // real mouse event (forced by the client when --forward-on-click)
            pointerProperties[pointerIndex].toolType = MotionEvent.TOOL_TYPE_MOUSE;
            source = InputDevice.SOURCE_MOUSE;
            pointer.setUp(buttons == 0);
        } else {
            // POINTER_ID_GENERIC_FINGER, POINTER_ID_VIRTUAL_FINGER or real touch from device
            pointerProperties[pointerIndex].toolType = MotionEvent.TOOL_TYPE_FINGER;
            source = InputDevice.SOURCE_TOUCHSCREEN;
            // Buttons must not be set for touch events
            buttons = 0;
            pointer.setUp(action == MotionEvent.ACTION_UP);
        }

        int pointerCount = pointersState.update(pointerProperties, pointerCoords);
        L.d("pointerCount -> " + pointerCount);
        if (pointerCount == 1) {
            if (action == MotionEvent.ACTION_DOWN) {
                lastTouchDown = now;
            }
        } else {
            // secondary pointers must use ACTION_POINTER_* ORed with the pointerIndex
            if (action == MotionEvent.ACTION_UP) {
                action = MotionEvent.ACTION_POINTER_UP | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            } else if (action == MotionEvent.ACTION_DOWN) {
                action = MotionEvent.ACTION_POINTER_DOWN | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            }
        }

        /* If the input device is a mouse (on API >= 23):
         *   - the first button pressed must first generate ACTION_DOWN;
         *   - all button pressed (including the first one) must generate ACTION_BUTTON_PRESS;
         *   - all button released (including the last one) must generate ACTION_BUTTON_RELEASE;
         *   - the last button released must in addition generate ACTION_UP.
         *
         * Otherwise, Chrome does not work properly: <https://github.com/Genymobile/scrcpy/issues/3635>
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && source == InputDevice.SOURCE_MOUSE) {
            if (action == MotionEvent.ACTION_DOWN) {
                if (actionButton == buttons) {
                    // First button pressed: ACTION_DOWN
                    MotionEvent downEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_DOWN, pointerCount, pointerProperties,
                            pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                    if (!injectEvent(downEvent, INJECT_MODE_ASYNC)) {
//                        L.d("injectTouch actionButton = buttons action = MotionEvent.ACTION_DOWN downEvent->" + downEvent);
                        return false;
                    }
                }

                // Any button pressed: ACTION_BUTTON_PRESS
                MotionEvent pressEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_BUTTON_PRESS, pointerCount, pointerProperties,
                        pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                if (!InputManagerSimulate.setActionButton(pressEvent, actionButton)) {
//                    L.d("injectTouch InputManagerSimulate.setActionButton action = MotionEvent.ACTION_DOWN");
                    return false;
                }
                if (!injectEvent(pressEvent, INJECT_MODE_ASYNC)) {
//                    L.d("injectTouch injectEvent(pressEvent, INJECT_MODE_ASYNC pressEvent->" + pressEvent);
                    return false;
                }

                return true;
            }

            if (action == MotionEvent.ACTION_UP) {
                // Any button released: ACTION_BUTTON_RELEASE
                MotionEvent releaseEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_BUTTON_RELEASE, pointerCount, pointerProperties,
                        pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                if (!InputManagerSimulate.setActionButton(releaseEvent, actionButton)) {
//                    L.d("injectTouch InputManagerSimulate.setActionButton action = MotionEvent.ACTION_UP");
                    return false;
                }
                if (!injectEvent(releaseEvent, INJECT_MODE_ASYNC)) {
//                    L.d("injectEvent(releaseEvent, INJECT_MODE_ASYNC) releaseEvent->" + releaseEvent);
                    return false;
                }

                if (buttons == 0) {
                    L.d("Last button released: ACTION_UP");
                    // Last button released: ACTION_UP
                    MotionEvent upEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_UP, pointerCount, pointerProperties,
                            pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                    if (!injectEvent(upEvent, INJECT_MODE_ASYNC)) {
//                        L.d("injectEvent(upEvent, INJECT_MODE_ASYNC) upEvent->" + upEvent);
                        return false;
                    }
                }

                return true;
            }
        }

        MotionEvent event = MotionEvent
                .obtain(lastTouchDown, now, action, pointerCount, pointerProperties, pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source,
                        0);
        L.d("injectEvent(event, INJECT_MODE_ASYNC) event->" + event);
        return injectEvent(event, INJECT_MODE_ASYNC);
    }

    public static boolean supportsInputEvents(int displayId) {
        return displayId == 0 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public boolean injectEvent(InputEvent event, int injectMode) {
        return injectEvent(event, displayId, injectMode);
    }

    public static boolean injectEvent(InputEvent inputEvent, int displayId, int injectMode) {
        if (!supportsInputEvents(displayId)) {
            throw new AssertionError("Could not inject input event if !supportsInputEvents()");
        }

        if (displayId != 0 && !InputManagerSimulate.setDisplayId(inputEvent, displayId)) {
            L.d("Could not set input event display id");
            return false;
        }

        return ServiceManager.getInputManager().injectInputEvent(inputEvent, injectMode);
    }
}
