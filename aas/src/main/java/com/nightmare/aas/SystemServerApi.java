package com.nightmare.aas;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;

import com.nightmare.aas.foundation.Singleton;

public class SystemServerApi {

    private static final Singleton<IPackageManager> PACKAGE_MANAGER = new Singleton<IPackageManager>() {
        @Override
        protected IPackageManager create() {
            return IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        }
    };


    public static final Singleton<IActivityManager> ACTIVITY_MANAGER = new Singleton<IActivityManager>() {
        @Override
        protected IActivityManager create() {
            IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                return IActivityManager.Stub.asInterface(binder);
            } else {
                return ActivityManagerNative.asInterface(binder);
            }
        }
    };


}

