package com.pusher.client.connection;

public interface ConnectionEventListener {

    void onConnectionStateChange(ConnectionStateChange change);
}