package com.nightmare.aas_plugins;

import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.L;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class CMDHandler extends AndroidAPIPlugin {
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
        String postData = body.get("postData");
//        L.d("body -> " + postData);
        Process process = null;
        StringBuilder stringBuilder = new StringBuilder();
        JSONObject jsonObject = new JSONObject();
        try {
            process = Runtime.getRuntime().exec(postData);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            PrintWriter writer = new PrintWriter(process.getOutputStream());

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            // 读取命令的错误输出
            while ((line = errorReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            // 等待命令执行完毕
            int exitCode = process.waitFor();
//            L.d("Exit Code: " + exitCode);

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
        try {
            jsonObject.put("result", stringBuilder.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "application/json",
                jsonObject.toString()
        );
    }
}
