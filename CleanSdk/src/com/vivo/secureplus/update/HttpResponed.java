package com.vivo.secureplus.update;

public interface HttpResponed {
    public static final int START_TO_CONNECT = 100;
    
    public static final int CONNECT_FAILED_CLIENT_ERROR = 201;
    public static final int CONNECT_FAILED_NETWORK_ERROR = 202;
    public static final int CONNECT_FAILED_NO_RESPONSE =  203;
    public static final int CONNECT_FAILED_NO_STATUS_CODE = 204;
    public static final int CONNECT_FAILED_WRONG_STATUS_CODE = 205;
    public static final int CONNECT_FAILED_GET_CONTENT_ERROR = 206;
    public static final int CONNECT_FAILED_CONNECTION_DISCONNECTION = 207;
    
    public static final int CONNECT_SUCCESS = 300;
    
    
    public void respond(HttpConnect connect, Object connId, int connStatus,/* String in,*/ Object response);
}