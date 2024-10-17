package com.nightmare.applib;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;

import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.ReflectUtil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;

import android.os.IBinder;

import com.nightmare.applib.utils.Workarounds;
import com.nightmare.applib.wrappers.ActivityManager;
import com.nightmare.applib.wrappers.ServiceManager;


public class SulaServer {
    private LocalServerSocket serverSocket;
    private static final String SOCKET_PATH = "/data/local/tmp" + "/local_socket";

    static private ServiceConnection serviceConnection = new SulaServiceConnection();

    /**
     * @noinspection JavaReflectionMemberAccess
     */
    static void start() {
        L.d("start unix socket server");
        ActivityManager activityManager = ActivityManager.create();
        ReflectUtil.listAllObject(activityManager);
        IInterface iInterface = ServiceManager.getService("activity", "android.app.IActivityManager");
        ReflectUtil.listAllObject(FakeContext.get().getApplicationContext());
//        IActivityManager iActivityManager = IActivityManager.Stub.asInterface(activityManager.getIActivityManager());
//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName("com.nightmare.sula", "com.nightmare.sula.services.SulaService"));
//        MyParcelable myParcelable = new MyParcelable(123);
//        intent.putExtra("my_parcelable", myParcelable);
//        FakeContext.get().startService(intent);
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(9999);
                while (true) {
                    java.net.Socket socket = serverSocket.accept();
                    InputStream inputStream = socket.getInputStream();
                    OutputStream outputStream = socket.getOutputStream();
                    try {
                        L.d("1");
                        DisplayManager displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
                        L.d("2");
//            ReflectUtil.listAllObject(displayManager);
                        // 反射获取 android.hardware.display.DisplayManagerGlobal mGlobal
                        Field mGlobal = displayManager.getClass().getDeclaredField("mGlobal");
                        mGlobal.setAccessible(true);
                        Object displayManagerGlobal = mGlobal.get(displayManager);
//            ReflectUtil.listAllObject(displayManagerGlobal);
                        // 反射获取mDm
                        Field mDm = displayManagerGlobal.getClass().getDeclaredField("mDm");
                        mDm.setAccessible(true);
                        Object dm = mDm.get(displayManagerGlobal);
//            ReflectUtil.listAllObject(dm);
                        // 调用 asBinder() 获取 IBinder
                        Method asBinder = dm.getClass().getMethod("asBinder");
                        IBinder binder = (IBinder) asBinder.invoke(dm);
                        L.d("binder: " + binder);
                        byte[] data = serializeIBinder(binder);
                        outputStream.write(data);
                        socket.close();
                    } catch (IllegalAccessException | InstantiationException |
                             InvocationTargetException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();


    }

    static public byte[] serializeIBinder(IBinder binder) {
        Parcel parcel = Parcel.obtain();
        parcel.writeStrongBinder(binder);

        byte[] data = parcel.marshall();
        parcel.recycle();
        return data;
    }
}
