package com.nightmare.applib.handler;

import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.L;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class CMDHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/cmd";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        Map<String, String> body = new HashMap<>();
        try {
            session.parseBody(body);
        } catch (IOException | NanoHTTPD.ResponseException e) {
            throw new RuntimeException(e);
        }
        L.d("body -> " + body.get("postData"));
        Process process = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec(body.get("postData"));

            // 处理命令的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            PrintWriter writer = new PrintWriter(process.getOutputStream());

            // 读取命令的标准输出
            String result = "";
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }

            // 读取命令的错误输出
            while ((line = errorReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }

            // 等待命令执行完毕
            int exitCode = process.waitFor();
            L.d("Exit Code: " + exitCode);

            // 关闭流
            reader.close();
            errorReader.close();
            writer.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }


        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", stringBuffer.toString());
    }
}
