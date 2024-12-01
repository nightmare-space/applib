package android.hardware.input;

import android.view.InputEvent;

public class InputManagerGlobal {

    /**
     * Gets an instance of the input manager global singleton.
     *
     * @return The input manager instance, may be null early in system startup
     * before the input manager has been fully initialized.
     */
    public static InputManagerGlobal getInstance() {
        throw new RuntimeException("STUB");
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        throw new RuntimeException("STUB");
    }
}
