package com.pusher.client.connection;

public interface ConnectionEventListener {

    void onConnectionStateChange(ConnectionStateChange change);
    
    void onError(String message, String code, Exception e);
}