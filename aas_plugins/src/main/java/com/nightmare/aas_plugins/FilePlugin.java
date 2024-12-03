package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;

import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.L;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class FilePlugin extends AndroidAPIPlugin {
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
                return handleFileBytes(session.getParms(), session);
            default:
                return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Invalid action parameter");
        }
    }

    private InputStream readFileBytes(File file) throws IOException {
        return new FileInputStream(file);
    }

    private NanoHTTPD.Response addCORSHeaders(NanoHTTPD.Response response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        return response;
    }


    private NanoHTTPD.Response handleFileBytes(Map<String, String> params, NanoHTTPD.IHTTPSession session) {
        String path = params.get("path");
        if (path == null) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Missing parameters");
        }
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", "File not found");
        }

        String rangeHeader = session.getHeaders().get("range");
        L.d("rangeHeader -> " + rangeHeader);
        L.d("session.getMethod() -> " + session.getMethod());
        long fileLength = file.length();
        L.d("fileLength -> " + fileLength);
        long start = 0;
        long end = fileLength - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            try {
                if (ranges.length > 0) {
                    start = Long.parseLong(ranges[0]);
                }
                if (ranges.length > 1) {
                    end = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                return newFixedLengthResponse(NanoHTTPD.Response.Status.RANGE_NOT_SATISFIABLE, "text/plain", "Invalid range");
            }
        }

        if (start > end || start < 0 || end >= fileLength) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.RANGE_NOT_SATISFIABLE, "text/plain", "Invalid range");
        }

        long contentLength = end - start + 1;
        L.d("start -> " + start + " end -> " + end);

        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        if (session.getMethod() == NanoHTTPD.Method.HEAD) {
            NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, mimeType, null, 0);
            response.addHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
            response.addHeader("Accept-Ranges", "bytes");
            return response;
        } else {

            try {
                NanoHTTPD.Response.Status status = NanoHTTPD.Response.Status.OK;
                if(rangeHeader != null && rangeHeader.startsWith("bytes=")){
                    status = NanoHTTPD.Response.Status.PARTIAL_CONTENT;
                }
                InputStream fileInputStream = new FileInputStream(file);
                fileInputStream.skip(start);
                NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(status, mimeType, fileInputStream, contentLength);
                response.addHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                response.addHeader("Accept-Ranges", "bytes");
                L.d("Content-Range -> " + "bytes " + start + "-" + end + "/" + fileLength);
                L.d("Content-Length -> " + contentLength);
//                response.addHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
                response = addCORSHeaders(response);
                return response;
            } catch (IOException e) {
                L.e("FileBytes Exception: " + e);
                return newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", "Error reading file");
            }
        }
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
