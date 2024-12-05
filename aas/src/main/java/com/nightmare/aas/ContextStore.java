package com.nightmare.aas;

import android.annotation.SuppressLint;
import android.content.Context;

public class ContextStore {
    @SuppressLint("StaticFieldLeak")
    private static final ContextStore INSTANCE = new ContextStore();

    private ContextStore() {
    }

    public static ContextStore getInstance() {
        return INSTANCE;
    }

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    static public Context getContext() {
        return INSTANCE.context;
    }

}
