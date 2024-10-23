package com.nightmare.applib.handler;

import com.nightmare.applib.interfaces.IHTTPHandler;

import fi.iki.elonen.NanoHTTPD;

public class ChangeDisplayHandler extends IHTTPHandler {
    @Override
    public String route() {
        return "change_display";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
//                DisplayManager displayManager = (DisplayManager) appChannel.context.getSystemService(Context.DISPLAY_SERVICE);
//                Display[] displays = displayManager.getDisplays();
//                Display currentDisplay = displays[0];
//                Display.Mode[] modes = new Display.Mode[0];
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    modes = currentDisplay.getSupportedModes();
//                }
////                currentDisplay.
//                Log.d("SecondaryActivityWithFloatWindow", "modes -> " + Arrays.toString(modes));
//                Display.Mode mode = modes[1]; // 选择第一个支持的模式
//                for (Display.Mode m : modes) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (m.getModeId() == 88) { // 选择刷新率为60Hz的模式
//                            mode = m;
//                            final Window window = getWindow();
//                            final WindowManager.LayoutParams params = window.getAttributes();
//                            Log.d("SecondaryActivityWithFloatWindow", "modes -> " + mode);
//                            params.preferredDisplayModeId = mode.getModeId();
//                            window.setAttributes(params);
//                            break;
//                        }
//                    }
//                }
        return null;
    }
}
