package com.nightmare.applib.handler;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;

import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.io.ByteArrayOutputStream;

import fi.iki.elonen.NanoHTTPD;

public class FileHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/file";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String action = session.getParms().get("action");
        if (action == null) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Missing action parameter");
        }
        switch (action) {
            case "dir":
                return handleDir(session.getParms());
            case "delete":
                return handleDelete(session.getParms());
            case "rename":
                return handleRename(session.getParms());
            case "token":
                return handleToken();
            case "upload":
                return handleFileUpload(session);
            case "file":
                return handleFileBytes(session.getParms());
            default:
                return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Invalid action parameter");
        }
    }

    private InputStream readFileBytes(File file) throws IOException {
        return new FileInputStream(file);
    }

    private NanoHTTPD.Response handleFileBytes(Map<String, String> params) {
        String path = params.get("path");
        if (path == null) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Missing parameters");
        }
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", "File not found");
        }
        try {
            InputStream fileInputStream = readFileBytes(file);
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, mimeType, fileInputStream, file.length());
        } catch (IOException e) {
            L.e("FileBytes Exception: " + e);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", "Error reading file");
        }
    }

    private NanoHTTPD.Response handleRename(Map<String, String> params) {
        String path = params.get("path");
        String name = params.get("name");
        if (path == null || name == null) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Missing parameters");
        }
        File source = new File(path);
        File dest = new File(source.getParent(), name);
        if (source.renameTo(dest)) {
            // File renamed successfully
        } else {
            // Handle error
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "success");
    }

    private NanoHTTPD.Response handleDelete(Map<String, String> params) {
        String path = params.get("path");
        if (path == null) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Missing parameters");
        }
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                // File deleted successfully
            } else {
                // Handle error
            }
        } else {
            // File does not exist
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "success");
    }

    private NanoHTTPD.Response handleDir(Map<String, String> params) {
        try {
            String path = params.get("path");
            if (path == null) {
                return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Missing parameters");
            }
            JSONArray dirInfos = getDirInfos(path);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", dirInfos.toString());
        } catch (Exception e) {
            L.e("DirHandler Exception: " + e);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", "Error getting directory information");
        }
    }


    private JSONArray getDirInfos(String path) throws IOException {
        File directory = new File(path);
        JSONArray infos = new JSONArray();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    JSONArray info = new JSONArray();
                    info.put(file.getPath());
                    info.put((file.canRead() ? "r" : "-") + (file.canWrite() ? "w" : "-") + (file.canExecute() ? "x" : "-"));
                    info.put(file.length());
                    long lastModifiedMillis = file.lastModified();
                    Date lastModifiedDate = new Date(lastModifiedMillis);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
                    String formattedDate = dateFormat.format(lastModifiedDate);
                    info.put(formattedDate);
                    info.put(file.isDirectory() ? "directory" : "file");
                    infos.put(info);
                }
            }
        }
        return infos;
    }

    private NanoHTTPD.Response handleToken() {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "success");
    }


    private NanoHTTPD.Response handleFileUpload(NanoHTTPD.IHTTPSession session) {
        try {
            Map<String, String> files = new HashMap<>();
            session.parseBody(files);
            String fileName = session.getHeaders().get("filename");
            String path = session.getHeaders().get("path");
            if (fileName == null || path == null) {
                return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Missing headers");
            }
            File file = new File(path, fileName);
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                InputStream inputStream = new FileInputStream(files.get("postData"));
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    raf.write(buffer, 0, bytesRead);
                }
            }
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "success");
        } catch (Exception e) {
            L.e("FileUpload Exception: " + e);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", "Error uploading file");
        }
    }
}
