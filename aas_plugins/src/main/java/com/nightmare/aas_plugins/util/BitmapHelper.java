package com.nightmare.aas_plugins.util;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class BitmapHelper {

    static public byte[] bitmap2Bytes(Bitmap bm) {
        if (bm == null) {
            return new byte[0];
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
