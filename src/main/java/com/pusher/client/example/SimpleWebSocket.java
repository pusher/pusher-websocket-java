package com.pusher.client.example;

import java.net.URI;
import java.net.URISyntaxException;

import com.pusher.java_websocket.client.WebSocketClient;
import com.pusher.java_websocket.handshake.ServerHandshake;

public class SimpleWebSocket extends WebSocketClient {
    
    public SimpleWebSocket() throws URISyntaxException {
        super(new URI("ws://ws.pusherapp.com/app/387954142406c3c9cc13?protocol=6&client=js&version=0.1.2&flash=false"));

        System.out.println("SimpleWebSocket");

        connect();
    }
    
    public static void main(final String[] args) throws URISyntaxException {
        new SimpleWebSocket();
    }

    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        System.out.println("onOpen");

    }

    @Override
    public void onMessage(final String message) {
        System.out.println("onMessage: " + message);
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        System.out.println("onClose");
    }

    @Override
    public void onError(final Exception ex) {
        System.out.println("onError");
    }
}
