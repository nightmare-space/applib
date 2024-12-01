package android.app;

import android.os.Build;
import android.window.TaskSnapshot;

import androidx.annotation.RequiresApi;

public class IActivityTaskManager {
    @RequiresApi(Build.VERSION_CODES.S)
    public TaskSnapshot getTaskSnapshot(int taskId, boolean isLowResolution) {
        throw new RuntimeException("Stub!");
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public TaskSnapshot getTaskSnapshot(int taskId, boolean what, boolean isLowResolution) {
        throw new RuntimeException("Stub!");
    }

    public TaskSnapshot takeTaskSnapshot(int taskId) {
        throw new RuntimeException("Stub!");
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public TaskSnapshot takeTaskSnapshot(int taskId, boolean isLowResolution) {
        throw new RuntimeException("Stub!");
    }
}
