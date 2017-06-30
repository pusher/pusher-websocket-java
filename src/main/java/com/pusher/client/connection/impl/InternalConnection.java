package com.pusher.client.connection.impl;

import com.pusher.client.connection.Connection;

public interface InternalConnection extends Connection {

    void sendMessage(String message);

    void disconnect();
}
