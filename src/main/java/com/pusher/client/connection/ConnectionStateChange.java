package com.pusher.client.connection;

public class ConnectionStateChange {

    private final ConnectionState previousState;
    private final ConnectionState currentState;

    ConnectionStateChange(ConnectionState previousState, ConnectionState currentState) {
	this.previousState = previousState;
	this.currentState = currentState;
    }

    public ConnectionState getPreviousState() {
	return previousState;
    }

    public ConnectionState getCurrentState() {
	return currentState;
    }
}