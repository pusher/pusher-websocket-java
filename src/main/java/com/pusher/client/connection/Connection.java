package com.pusher.client.connection;

public interface Connection {

    void connect(String apiKey);

    void setEventListener(ConnectionEventListener eventListener);
    
    void bind(ConnectionState state, ConnectionEventListener eventListener);
}