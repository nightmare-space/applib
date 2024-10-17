package com.nightmare.applib;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

class SulaServiceConnection implements ServiceConnection {
    public void onServiceDisconnected(ComponentName name) {
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
    }
}