package com.pusher.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.connection.Connection;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.util.Factory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class PusherTest {

    private static final String API_KEY = "123456";
    private Pusher pusher;
    private @Mock Connection mockConnection;
    private @Mock ConnectionEventListener mockConnectionEventListener;
    
    @Before
    public void setUp()
    {
	PowerMockito.mockStatic(Factory.class);
	when(Factory.newConnection(API_KEY)).thenReturn(mockConnection);
	
	this.pusher = new Pusher(API_KEY);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullAPIKeyThrowsIllegalArgumentException() {
	new Pusher(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyAPIKeyThrowsIllegalArgumentException() {
	new Pusher("");
    }
    
    @Test
    public void testCreatesConnectionObjectWhenConstructed() {
	assertNotNull(pusher.getConnection());
	assertSame(mockConnection, pusher.getConnection());
    }
    
    @Test
    public void testConnectCallIsDelegatedToUnderlyingConnection() {
	pusher.connect();
	verify(mockConnection).connect();
    }
    
    @Test
    public void testConnectCallWithListenerIsDelegatedToUnderlyingConnection() {
	pusher.connect(mockConnectionEventListener);
	
	InOrder inOrder = inOrder(mockConnection);
	inOrder.verify(mockConnection).setEventListener(mockConnectionEventListener);
	inOrder.verify(mockConnection).connect();
    }    
}