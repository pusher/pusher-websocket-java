package com.pusher.client.example;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class SimpleWebSocket extends WebSocketClient {
    public static void main(String[] args) throws URISyntaxException {
        new SimpleWebSocket();
    }

    public SimpleWebSocket() throws URISyntaxException {
        super(new URI("ws://ws.pusherapp.com/app/387954142406c3c9cc13?protocol=6&client=js&version=0.1.2&flash=false"));

        System.out.println("SimpleWebSocket");

        this.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("onOpen");

    }

    @Override
    public void onMessage(String message) {
        System.out.println("onMessage: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("onClose");
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("onError");
    }
}
