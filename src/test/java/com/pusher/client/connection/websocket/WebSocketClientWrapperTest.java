package com.pusher.client.connection.websocket;

import static org.mockito.Mockito.verify;

import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLException;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketClientWrapperTest {
    
    private WebSocketClientWrapper wrapper;
    private @Mock WebSocketListener mockProxy;
    private @Mock ServerHandshake mockHandshake;
    
    @Before
    public void setUp() throws URISyntaxException, SSLException {
	wrapper = new WebSocketClientWrapper(new URI("http://www.test.com"), mockProxy);
    }
    
    @Test
    public void testOnOpenCallIsDelegatedToTheProxy() {
	wrapper.onOpen(mockHandshake);
	verify(mockProxy).onOpen(mockHandshake);
    }
    
    @Test
    public void testOnMessageIsDelegatedToTheProxy() {
	wrapper.onMessage("hello");
	verify(mockProxy).onMessage("hello");
    }
    
    @Test
    public void testOnCloseIsDelegatedToTheProxy() {
	wrapper.onClose(1, "reason", true);
	verify(mockProxy).onClose(1, "reason", true);
    }
    
    @Test
    public void testOnErrorIsDelegatedToTheProxy() {
	Exception e = new Exception();
	wrapper.onError(e);
	verify(mockProxy).onError(e);
    }
}