package com.pusher.client.connection;

public interface Connection {

    void connect();

    void setEventListener(ConnectionEventListener eventListener);
    
    void bind(ConnectionState state, ConnectionEventListener eventListener);
    
    ConnectionState getState();
}