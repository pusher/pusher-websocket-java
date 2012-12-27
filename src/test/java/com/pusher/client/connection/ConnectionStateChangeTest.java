package com.pusher.client.connection;

import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ConnectionStateChangeTest {

    @Test
    public void testGetters() {
	ConnectionState previous = ConnectionState.DISCONNECTED;
	ConnectionState current = ConnectionState.CONNECTING;
	
	ConnectionStateChange change = new ConnectionStateChange(previous, current);
	
	assertSame(previous, change.getPreviousState());
	assertSame(current, change.getCurrentState());
    }
}