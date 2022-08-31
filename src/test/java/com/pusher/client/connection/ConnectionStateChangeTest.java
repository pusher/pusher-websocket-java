package com.pusher.client.connection;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionStateChangeTest {

    @Test
    public void testGetters() {
        final ConnectionState previous = ConnectionState.DISCONNECTED;
        final ConnectionState current = ConnectionState.CONNECTING;

        final ConnectionStateChange change = new ConnectionStateChange(
                previous,
                current
        );

        assertSame(previous, change.getPreviousState());
        assertSame(current, change.getCurrentState());
    }

    @Test
    public void testHashCodeAndEquals() {
        final ConnectionStateChange instanceOne = new ConnectionStateChange(
                ConnectionState.DISCONNECTED,
                ConnectionState.CONNECTING
        );
        final ConnectionStateChange instanceTwo = new ConnectionStateChange(
                ConnectionState.DISCONNECTED,
                ConnectionState.CONNECTING
        );

        assertTrue(instanceOne.hashCode() == instanceTwo.hashCode());
        assertTrue(instanceOne.equals(instanceTwo));
    }
}
