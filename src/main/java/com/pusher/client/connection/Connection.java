package com.pusher.client.connection;

public interface Connection {

    void connect();

    void bind(ConnectionState state, ConnectionEventListener eventListener);
    
    boolean unbind(ConnectionState state, ConnectionEventListener eventListener);
    
    ConnectionState getState();
    
    String getSocketId();
}