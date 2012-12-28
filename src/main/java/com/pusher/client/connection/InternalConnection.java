package com.pusher.client.connection;

public interface InternalConnection extends Connection {

    void sendMessage(String message);
}