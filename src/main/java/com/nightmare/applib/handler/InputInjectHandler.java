package com.nightmare.applib.handler;

import com.nightmare.applib.InputDispatcher;
import com.nightmare.applib.Position;
import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.Binary;
import com.nightmare.applib.utils.L;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import fi.iki.elonen.NanoHTTPD;

public class InputInjectHandler extends IHTTPHandler {
    public InputInjectHandler() {
        new Thread(() -> {
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
                        L.d("Sula input Wait Client Connect");
                        Socket socket = serverSocket.accept();
                        L.d("Sula input has connected");
                        InputStream in = socket.getInputStream();
//                            OutputStream out = socket.getOutputStream();
                        new Thread(() -> {
                            L.d("Sula input Handle socket in new Thread");
                            handleSocket(in);
                        }).start();
                    } catch (IOException e) {
                        L.d("startInputDispatcher error" + e);
                    }
                }

            } catch (IOException e) {
                L.d("startInputDispatcher error" + e);
            }
        }).start();
    }
    static InputDispatcher inputDispatcher = new InputDispatcher();
    public static final int TYPE_INJECT_KEYCODE = 0;
    public static final int TYPE_INJECT_TOUCH_EVENT = 2;
    static final int INJECT_KEYCODE_PAYLOAD_LENGTH = 13;
    static final int INJECT_TOUCH_EVENT_PAYLOAD_LENGTH = 31;

    // 目前暂时支持两种协议，后续考虑直接复用 scrcpy-server
    // 目前看是不能复用 scrcpy-server, scrcpy 在投屏开始便指定了 display id
    // applib server 需要支持往不同的 display 中下发事件
    void handleSocket(InputStream in) {
        while (true) {
            // 持续读出数据
            int type = -1;
            try {
                type = in.read();
            } catch (IOException e) {
                L.d("in.read e : " + e);
            }
            if (type == -1) {
                break;
            }
            L.d("Sula input type : " + type);
            switch (type) {
                case TYPE_INJECT_KEYCODE:
                    handleKeyEvent(in);
                    break;
                case TYPE_INJECT_TOUCH_EVENT:
                    handleTouchEvent(in);
                    break;
            }

        }
    }

    void handleKeyEvent(InputStream in) {
        byte[] data = new byte[INJECT_KEYCODE_PAYLOAD_LENGTH];
        int bytesRead = 0;
//                            L.d("in.read start");
        try {
            bytesRead = in.read(data);
        } catch (IOException e) {
            L.d("in.read e : " + e);
        }
        if (bytesRead == -1) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        // 1 byte
        int action = buffer.get();
        // 4 bytes total 5 bytes
        int keycode = buffer.getInt();
        // 4 bytes total 9 bytes
        int repeat = buffer.getInt();
        // 4 bytes total 13 bytes
        int metaState = buffer.getInt();
        boolean success = inputDispatcher.injectKeyEvent(action, keycode, repeat, metaState, InputDispatcher.INJECT_MODE_ASYNC);
    }

    void handleTouchEvent(InputStream in) {
        byte[] data = new byte[INJECT_TOUCH_EVENT_PAYLOAD_LENGTH];
        int bytesRead = 0;
//        L.d("in.read start");
        try {
            bytesRead = in.read(data);
        } catch (IOException e) {
            L.d("in.read e : " + e);
        }
        if (bytesRead == -1) {
            return;
        }
//        L.d("Sula input Received : " + Arrays.toString(data));
        ByteBuffer buffer = ByteBuffer.wrap(data);
        // 1 byte
        int actionInt = buffer.get();
        // 1 byte total 2 bytes
        // 为什么 value & 0xffff; 可以把有符号转成无符号
        int displayIdInt = Binary.toUnsigned(buffer.getShort());
        // 4 bytes total 6 bytes
        long pointerIdInt = buffer.getInt();
        // 4 bytes total 10 bytes
        int xInt = buffer.getInt();
        // 4 bytes total 14 bytes
        int yInt = buffer.getInt();
        // 4 bytes total 18 bytes
        int widthInt = buffer.getInt();
        // 4 bytes total 22 bytes
        int heightInt = buffer.getInt();
        // 4 bytes total 26 bytes
        int actionButtonInt = buffer.getInt();
        // 4 bytes total 30 bytes
        int buttonsInt = buffer.getInt();

        L.d("Sula input Received : d" + displayIdInt + " a" + actionInt + " p" + pointerIdInt + " x" + xInt
                + " y" + yInt + " w" + widthInt + " h" + heightInt + " ab" + actionButtonInt + " b" + buttonsInt);
        Position position = new Position(xInt, yInt, widthInt, heightInt);
        inputDispatcher.setDisplayId(displayIdInt);
        float pressure = 0f;
        // if (actionInt == MotionEvent.ACTION_DOWN || actionInt ==
        // MotionEvent.ACTION_MOVE) {
        // pressure = 1f;
        // }
        boolean success = inputDispatcher.injectTouch(actionInt, pointerIdInt, position, pressure, actionButtonInt, buttonsInt);
    }
    @Override
    public String route() {
        return "";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        return null;
    }
}
