package com.nightmare.applib.wrappers;

import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;

import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.ReflectUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//InputManagerSimulate
//InputManagerProxy
//InputManagerDelegate
//InputManagerReflector
public final class InputManagerSimulate {

    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

    private final Object manager;
    private Method injectInputEventMethod;

    private static Method setDisplayIdMethod;
    private static Method setActionButtonMethod;

    public InputManagerSimulate(Object manager) {
        this.manager = manager;
    }


    private Method getInjectInputEventMethod() throws NoSuchMethodException {
        if (injectInputEventMethod == null) {
            injectInputEventMethod = manager.getClass().getMethod("injectInputEvent", InputEvent.class, int.class);
        }
        return injectInputEventMethod;
    }

    public boolean injectInputEvent(InputEvent inputEvent, int mode) {
        try {
            Method method = getInjectInputEventMethod();
            return (boolean) method.invoke(manager, inputEvent, mode);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            L.d("Could not invoke method" + e);
            return false;
        }
    }

    private static Method getSetDisplayIdMethod() throws NoSuchMethodException {
        if (setDisplayIdMethod == null) {
            setDisplayIdMethod = InputEvent.class.getMethod("setDisplayId", int.class);
        }
        return setDisplayIdMethod;
    }

    public static boolean setDisplayId(InputEvent inputEvent, int displayId) {
        try {
            Method method = getSetDisplayIdMethod();
            method.invoke(inputEvent, displayId);
//            L.d("setDisplayId displayId: " + displayId + " inputEvent: " + inputEvent);
            return true;
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            L.d("Cannot associate a display id to the input event" + e);
            return false;
        }
    }

//    private Method getSetCustomPointerIconMethod() throws NoSuchMethodException {
//        if (setDisplayIdMethod == null) {
//            setDisplayIdMethod = manager.getClass().getMethod("setCustomPointerIcon", PointerIcon.class);
//        }
//        return setDisplayIdMethod;
//    }

//    public boolean setCustomPointerIcon(PointerIcon pointerIcon) {
//        try {
//            Method method = getSetCustomPointerIconMethod();
//            method.invoke(manager, pointerIcon);
////            Method method1 = manager.getClass().getMethod("setPointerSpeedUnchecked", int.class);
////            method1.invoke(manager, 100);
////            Method method1 = manager.getClass().getMethod("setPointerIconType", int.class);
////            Object o = method1.invoke(manager, 1002);
////            Method method4 = manager.getClass().getMethod("get", int.class);
////            method4.invoke(manager, 0);
////
////            Method method2 = manager.getClass().getMethod("setPointerAcceleration", float.class, int.class);
////            method2.invoke(manager, 2000000.0, 0);
////            Method method3 = manager.getClass().getMethod("getPointerSpeedSetting");
////            Object object = method3.invoke(manager);
////            L.d(object);
//            return true;
//        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
//            L.d("Cannot associate a display id to the input event" + e);
//            return false;
//        }
//    }


    private static Method getSetActionButtonMethod() throws NoSuchMethodException {
        if (setActionButtonMethod == null) {
            setActionButtonMethod = MotionEvent.class.getMethod("setActionButton", int.class);
        }
        return setActionButtonMethod;
    }

    public static boolean setActionButton(MotionEvent motionEvent, int actionButton) {
        try {
            Method method = getSetActionButtonMethod();
            method.invoke(motionEvent, actionButton);
            return true;
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            L.d("Cannot set action button on MotionEvent" + e);
            return false;
        }
    }
}