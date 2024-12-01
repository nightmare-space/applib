package android.hardware.display;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IDisplayManager extends IInterface {

    int[] getDisplayIds(boolean includeDisabled);

    abstract class Stub extends Binder implements IDisplayManager {

        public static IDisplayManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
