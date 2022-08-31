package com.pusher.client.connection.websocket;

import static org.mockito.Mockito.verify;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.verification.NoMoreInteractions;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLException;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketClientWrapperTest {

    private WebSocketClientWrapper wrapper;

    @Mock
    private WebSocketListener mockListener;

    @Mock
    private ServerHandshake mockHandshake;

    private Proxy mockProxy = Proxy.NO_PROXY;

    @Before
    public void setUp() throws URISyntaxException, SSLException {
        wrapper =
                new WebSocketClientWrapper(
                        new URI("http://www.test.com"),
                        mockProxy,
                        mockListener
                );
    }

    @Test
    public void testOnOpenCallIsDelegatedToTheListener() {
        wrapper.onOpen(mockHandshake);
        verify(mockListener).onOpen(mockHandshake);
    }

    @Test
    public void testhandleEventIsDelegatedToTheListener() {
        wrapper.onMessage("hello");
        verify(mockListener).onMessage("hello");
    }

    @Test
    public void testOnCloseIsDelegatedToTheListener() {
        wrapper.onClose(1, "reason", true);
        verify(mockListener).onClose(1, "reason", true);
    }

    @Test
    public void testOnErrorIsDelegatedToTheListener() {
        final Exception e = new Exception();
        wrapper.onError(e);
        verify(mockListener).onError(e);
    }

    @Test
    public void testRemoveWebSocketListener() {
        wrapper.onClose(1, "reason", true);
        verify(mockListener).onClose(1, "reason", true);

        wrapper.removeWebSocketListener();

        wrapper.onClose(1, "reason", true);
        verify(mockListener, new NoMoreInteractions()).onClose(1, "reason", true);
    }
}
