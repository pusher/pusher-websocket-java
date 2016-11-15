package com.pusher.client.connection.websocket;

import com.pusher.java_websocket.handshake.ServerHandshake;

public interface WebSocketListener {

    void onOpen(ServerHandshake handshakedata);

    void onMessage(String message);

    void onClose(int code, String reason, boolean remote);

    void onError(Exception ex);
}
