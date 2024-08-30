package com.nightmare.applib;

import static com.nightmare.applib.handler.InjectInputEvent.inputDispatcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import com.nightmare.applib.handler.*;
import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.ServerUtil;
import com.nightmare.applib.utils.Workarounds;

/**
 * 基于HTTP服务提供能力
 *
 * @noinspection deprecation
 */
public class AppServer extends NanoHTTPD {
    public AppServer(String address, int port) {
        super(address, port);
    }

    static public AppChannel appChannel;
    List<IHTTPHandler> handlers = new ArrayList<>();

    void addHandler(IHTTPHandler handler) {
        handlers.add(handler);
    }


    @SuppressLint("SdCardPath")
    public static void main(String[] args) throws Exception {
        L.d("Welcome!!!");
        L.d("args -> " + Arrays.toString(args));
        AppServer server = ServerUtil.safeGetServerForADB();
        L.d("Sula input socket server starting.");
        assert server != null;
        Workarounds.apply(true, true);
        server.startInputDispatcher();
        server.registerRoutes();
        // 获取安卓版本
        String sdk = Build.VERSION.SDK;
        String release = Build.VERSION.RELEASE;
        L.d("Info: Android " + release + "(" + sdk + ")");
        // 获取设备的型号信息
        String manufacturer = Build.MANUFACTURER; // 获取设备制造商，例如 "Samsung"
        String model = Build.MODEL; // 获取设备型号，例如 "Galaxy S10"
        // 构建显示信息
        String deviceInfo = "Info: " + manufacturer + "(" + model + ")";
        L.d(deviceInfo);
        // 这个时候构造的是一个没有 Context 的 Channel
        appChannel = new AppChannel();
        L.d("success start port -> " + server.getListeningPort() + ".");
        writePort("/sdcard", server.getListeningPort());
        // 让进程等待
        // 不能用 System.in.read(), System.in.read()需要宿主进程由标准终端调用
        // Process.run 等方法就会出现异常，
        // System.in.read();
        //noinspection InfiniteLoopStatement
        while (true) {
            //noinspection BusyWait
            Thread.sleep(1000);
        }
    }

    void registerRoutes() {
        addHandler(new AppActivityHandler());
        addHandler(new AppDetailHandler());
        addHandler(new AppInfoHandler());
        addHandler(new AppInfosHandler());
//        addHandler(new AppInfosHandlerV1());
        addHandler(new Appmainactivity());
        addHandler(new AppPermissionHandler());
        addHandler(new ChangeDisplayHandler());
        addHandler(new CMDHandler());
        addHandler(new CreateVirtualDisplay());
        addHandler(new CreateVirtualDisplayV2());
        addHandler(new CreateVirtualDisplayWithSurfaceView());
        addHandler(new DisplaysHandler());
        addHandler(new IconHandler());
        addHandler(new InjectInputEvent());
        addHandler(new OpenAppHandler());
        addHandler(new Resizevd());
        addHandler(new StopActivityHandler());
        addHandler(new TaskHandler());
        addHandler(new Taskthumbnail());
    }


    void startInputDispatcher() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    L.d("Sula input Thread run");
                    ServerSocket serverSocket = new ServerSocket(12345);
                    L.d("Sula input socket server started");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    while (true) {
                        try {
                            Socket socket = serverSocket.accept();
                            L.d("Sula input has connected");
                            InputStream in = socket.getInputStream();
                            OutputStream out = socket.getOutputStream();
                            handleSocket(in);
                        } catch (IOException e) {
                            L.d("startInputDispatcher error" + e);
                        }
                    }

                } catch (IOException e) {
                    L.d("startInputDispatcher error" + e);
                }
            }
        }).start();
    }

    void handleSocket(InputStream in) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // 持续读出数据
                    byte[] data = new byte[36];
                    int bytesRead = 0;

//                    L.d("in.read start");
                    try {
                        bytesRead = in.read(data);
                    } catch (IOException e) {
                        L.d("in.read e : " + e);
                    }
                    if (bytesRead == -1) {
                        break;
                    }
//                    L.d("Sula input Received : " + Arrays.toString(data));
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    int displayIdInt = buffer.getInt();
                    int actionInt = buffer.getInt();
                    long pointerIdInt = buffer.getInt();
                    int xInt = buffer.getInt();
                    int yInt = buffer.getInt();
                    int widthInt = buffer.getInt();
                    int heightInt = buffer.getInt();
                    int actionButtonInt = buffer.getInt();
                    int buttonsInt = buffer.getInt();
                    L.d("Sula input Received : " + displayIdInt + " " + actionInt + " " + pointerIdInt + " " + xInt
                            + " " + yInt + " " + widthInt + " " + heightInt + " " + actionButtonInt + " " + buttonsInt);
                    Position position = new Position(xInt, yInt, widthInt, heightInt);
                    inputDispatcher.setDisplayId(displayIdInt);
                    float pressure = 0f;
                    // if (actionInt == MotionEvent.ACTION_DOWN || actionInt ==
                    // MotionEvent.ACTION_MOVE) {
                    // pressure = 1f;
                    // }
                    boolean success = inputDispatcher.injectTouch(actionInt, pointerIdInt, position, pressure,
                            actionButtonInt, buttonsInt);

                }
            }
        }).start();
    }

    /**
     * 与直接启动dex不同，从Activity中启动不用反射context上下文
     *
     * @param context: Context
     * @throws IOException: IOException
     */
    public static void startServerFromActivity(Context context) throws IOException {
        AppServer server = ServerUtil.safeGetServer();
        // TODO 在确认下这个断言在 release 下是怎么的
        assert server != null;
        writePort(context.getFilesDir().getPath(), server.getListeningPort());
        server.appChannel = new AppChannel(context);
        System.out.println("success start:" + server.getListeningPort());
        System.out.flush();
    }

    /**
     * 写入端口号，方便不同进程同App，获得这个端口号
     *
     * @param path: 写入的路径
     * @param port: 端口号
     * @noinspection CallToPrintStackTrace
     */
    public static void writePort(String path, int port) {
        OutputStream out;
        try {
            String filePath = path + "/server_port";
            out = new FileOutputStream(filePath);
            L.d("port file path-> " + filePath);
            out.write((port + "").getBytes());
            out.close();
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public Object getParams(IHTTPSession session) {
        Map<String, List<String>> params = session.getParameters();
        return params.get(0);
    }


    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String url = session.getUri();
            // 获取最近任务
            for (IHTTPHandler handler : handlers) {
                if (url.startsWith(handler.route())) {
//                    L.d("url -> " + url);
//                    L.d("handler -> " + handler);
                    return handler.handle(session);
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "route not found");
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
        }
    }
}
