package com.pusher.client.connection;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
    
    @Test(expected=IllegalArgumentException.class)
    public void testFailFastIfPreviousStateIsSameAsCurrentState() {
	new ConnectionStateChange(ConnectionState.CONNECTED, ConnectionState.CONNECTED);
    }
    
    @Test
    public void testHashCodeAndEquals() {
	ConnectionStateChange instanceOne = new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING);
	ConnectionStateChange instanceTwo = new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING);
	
	assertTrue(instanceOne.hashCode() == instanceTwo.hashCode());
	assertTrue(instanceOne.equals(instanceTwo));
    }
}