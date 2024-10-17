package com.nightmare.applib.interfaces;

import fi.iki.elonen.NanoHTTPD;

public interface IHTTPHandler {

    public String route() ;

    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session);
}
