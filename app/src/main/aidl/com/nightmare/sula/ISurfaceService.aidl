// ISurfaceService.aidl
package com.nightmare.sula;

// Declare any non-default types here with import statements
import android.view.Surface;

interface ISurfaceService {
    void sendSurface(in Surface surface);
}