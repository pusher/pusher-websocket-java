package com.pusher.client.connection;

public class ConnectionStateChange {

    private final ConnectionState previousState;
    private final ConnectionState currentState;

    public ConnectionStateChange(ConnectionState previousState, ConnectionState currentState) {
	
	if(previousState == currentState) {
	    throw new IllegalArgumentException("Attempted to create an connection state update where both previous and current state are: " + currentState);
	}
	
	this.previousState = previousState;
	this.currentState = currentState;
    }

    public ConnectionState getPreviousState() {
	return previousState;
    }

    public ConnectionState getCurrentState() {
	return currentState;
    }
    
    @Override
    public int hashCode() {
	return previousState.hashCode() + currentState.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
	if(obj != null && obj instanceof ConnectionStateChange) {
	    ConnectionStateChange other = (ConnectionStateChange) obj;
	    return (this.currentState == other.currentState) && (this.previousState == other.previousState);
	}
	
	return false;
    }
}