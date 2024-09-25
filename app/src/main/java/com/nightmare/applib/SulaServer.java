package com.nightmare.applib;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.ParcelFileDescriptor;

import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.ReflectUtil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

public class SulaServer {
    private LocalServerSocket serverSocket;
    private static final String SOCKET_PATH = "/data/local/tmp" + "/local_socket";


    static void start() {
        L.d("start unix socket server");
    }

}
