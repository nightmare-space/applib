package com.nightmare.aas.foundation;


import fi.iki.elonen.NanoHTTPD;

public abstract class AndroidAPIPlugin {

    public String route() {
        return "_";
    }

    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session){
        return null;
    }

    @Override
    public String toString() {
        // return class name
        return this.getClass().getSimpleName();
    }
}
