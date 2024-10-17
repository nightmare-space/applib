package com.nightmare.applib.wrappers;

import com.nightmare.applib.Size;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class DisplayInfo {
//    private static final long serialVersionUID = 1L;

    private final int displayId;
    private final Size size;
    private final int rotation;
    private final int layerStack;
    private final int flags;
    private final String name;

    private final String dump;

    public static final int FLAG_SUPPORTS_PROTECTED_BUFFERS = 0x00000001;

    public DisplayInfo(int displayId, Size size, int rotation, int layerStack, int flags, String name, String dump) {
        this.displayId = displayId;
        this.size = size;
        this.rotation = rotation;
        this.layerStack = layerStack;
        this.flags = flags;
        this.name = name;
        this.dump = dump;
    }

    public int getDisplayId() {
        return displayId;
    }

    public Size getSize() {
        return size;
    }

    public int getRotation() {
        return rotation;
    }

    public int getLayerStack() {
        return layerStack;
    }

    public int getFlags() {
        return flags;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("displayId", displayId);
        Map<String, Integer> sizeMap = new HashMap<>();
        sizeMap.put("width", size.getWidth());
        sizeMap.put("height", size.getHeight());
        jsonMap.put("size", sizeMap);
        jsonMap.put("rotation", rotation);
        jsonMap.put("layerStack", layerStack);
        jsonMap.put("flags", flags);
        jsonMap.put("name", name);
        jsonMap.put("dump", dump);
        return jsonMap;
    }

    @Override
    public String toString() {
        return "DisplayInfo{" +
                "displayId=" + displayId +
                ", size=" + size +
                ", rotation=" + rotation +
                ", layerStack=" + layerStack +
                ", flags=" + flags +
                ", name='" + name +
                ", dump='" + dump +
                '\'' +
                '}';
    }

}
