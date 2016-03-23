package com.pusher.client.connection.websocket;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.pusher.client.channel.impl.ChannelManager;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.DoNothingExecutor;
import com.pusher.client.util.Factory;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionTest {

    private static final long ACTIVITY_TIMEOUT = 120000;
    private static final long PONG_TIMEOUT = 30000;
    private static final String URL = "ws://ws.example.com/";
    private static final String EVENT_NAME = "my-event";
    private static final String CONN_ESTABLISHED_EVENT = "{\"event\":\"pusher:connection_established\",\"data\":\"{\\\"socket_id\\\":\\\"21112.816204\\\"}\"}";
    private static final String INCOMING_MESSAGE = "{\"event\":\"" + EVENT_NAME
            + "\",\"channel\":\"my-channel\",\"data\":{\"fish\":\"chips\"}}";
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxyaddress", 80));

    @Mock
    private ChannelManager mockChannelManager;
    @Mock
    private WebSocketClientWrapper mockUnderlyingConnection;
    @Mock
    private ConnectionEventListener mockEventListener;
    @Mock
    private Factory factory;

    private WebSocketConnection connection;

    @Before
    public void setUp() throws URISyntaxException, SSLException {
        when(factory.getChannelManager()).thenReturn(mockChannelManager);
        when(factory.newWebSocketClientWrapper(any(URI.class), any(Proxy.class), any(WebSocketConnection.class))).thenReturn(
                mockUnderlyingConnection);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Runnable r = (Runnable) invocation.getArguments()[0];
                r.run();
                return null;
            }
        }).when(factory).queueOnEventThread(any(Runnable.class));
        when(factory.getTimers()).thenReturn(new DoNothingExecutor());

        connection = new WebSocketConnection(URL, ACTIVITY_TIMEOUT, PONG_TIMEOUT, PROXY, factory);
        connection.bind(ConnectionState.ALL, mockEventListener);
    }

    @Test
    public void testUnbindingWhenNotAlreadyBoundReturnsFalse() throws URISyntaxException {
        final ConnectionEventListener listener = mock(ConnectionEventListener.class);
        final WebSocketConnection connection = new WebSocketConnection(URL, ACTIVITY_TIMEOUT, PONG_TIMEOUT, PROXY, factory);
        final boolean unbound = connection.unbind(ConnectionState.ALL, listener);
        assertEquals(false, unbound);
    }

    @Test
    public void testUnbindingWhenBoundReturnsTrue() throws URISyntaxException {
        final ConnectionEventListener listener = mock(ConnectionEventListener.class);
        final WebSocketConnection connection = new WebSocketConnection(URL, ACTIVITY_TIMEOUT, PONG_TIMEOUT, PROXY, factory);

        connection.bind(ConnectionState.ALL, listener);

        final boolean unbound = connection.unbind(ConnectionState.ALL, listener);
        assertEquals(true, unbound);
    }

    @Test
    public void testStartsInDisconnectedState() {
        assertSame(ConnectionState.DISCONNECTED, connection.getState());
    }

    @Test
    public void testConnectCallIsDelegatedToUnderlyingConnection() {
        connection.connect();
        verify(mockUnderlyingConnection).connect();
    }

    @Test
    public void testConnectUpdatesStateAndNotifiesListener() {
        connection.connect();
        verify(mockEventListener).onConnectionStateChange(
                new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING));
        assertEquals(ConnectionState.CONNECTING, connection.getState());
    }

    @Test
    public void testConnectDoesNotCallConnectOnUnderlyingConnectionIfAlreadyInConnectingState() {
        connection.connect();
        connection.connect();

        verify(mockUnderlyingConnection, times(1)).connect();
        verify(mockEventListener, times(1)).onConnectionStateChange(any(ConnectionStateChange.class));
    }

    @Test
    public void testListenerDoesNotReceiveConnectingEventIfItIsOnlyBoundToTheConnectedEvent() throws URISyntaxException {
        connection = new WebSocketConnection(URL, ACTIVITY_TIMEOUT, PONG_TIMEOUT, PROXY, factory);
        connection.bind(ConnectionState.CONNECTED, mockEventListener);
        connection.connect();

        verify(mockEventListener, never()).onConnectionStateChange(any(ConnectionStateChange.class));
    }

    @Test
    public void testReceivePusherConnectionEstablishedMessageIsTranslatedToAConnectedCallback() {
        connection.connect();
        verify(mockEventListener).onConnectionStateChange(
                new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING));

        connection.onMessage(CONN_ESTABLISHED_EVENT);
        verify(mockEventListener).onConnectionStateChange(
                new ConnectionStateChange(ConnectionState.CONNECTING, ConnectionState.CONNECTED));

        assertEquals(ConnectionState.CONNECTED, connection.getState());
    }

    @Test
    public void testReceivePusherConnectionEstablishedMessageSetsSocketId() {
        assertNull(connection.getSocketId());

        connection.connect();
        connection.onMessage(CONN_ESTABLISHED_EVENT);

        assertEquals("21112.816204", connection.getSocketId());
    }

    @Test
    public void testReceivePusherErrorMessageRaisesErrorEvent() {
        connection.connect();
        verify(mockEventListener).onConnectionStateChange(
                new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING));

        connection
                .onMessage("{\"event\":\"pusher:error\",\"data\":{\"code\":4001,\"message\":\"Could not find app by key 12345\"}}");
        verify(mockEventListener).onError("Could not find app by key 12345", "4001", null);
    }

    @Test
    public void testSendMessageSendsMessageToPusher() {
        connect();

        connection.sendMessage("message");

        verify(mockUnderlyingConnection).send("message");
    }

    @Test
    public void testSendMessageWhenNotConnectedRaisesErrorEvent() {
        connection.sendMessage("message");

        verify(mockUnderlyingConnection, never()).send("message");
        verify(mockEventListener).onError(
                "Cannot send a message while in " + ConnectionState.DISCONNECTED.toString() + " state", null, null);
    }

    @Test
    public void testSendMessageWhenWebSocketLibraryThrowsExceptionRaisesErrorEvent() {
        connect();

        final RuntimeException e = new RuntimeException();
        doThrow(e).when(mockUnderlyingConnection).send(anyString());

        connection.sendMessage("message");

        verify(mockEventListener).onError("An exception occurred while sending message [message]", null, e);
    }

    @Test
    public void testReceiveUserMessagePassesMessageToChannelManager() {
        connect();

        connection.onMessage(INCOMING_MESSAGE);

        verify(mockChannelManager).onMessage(EVENT_NAME, INCOMING_MESSAGE);
    }

    @Test
    public void testOnCloseCallbackUpdatesStateToDisconnected() {
        connection.connect();
        verify(mockEventListener).onConnectionStateChange(
                new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING));

        connection.onClose(1, "reason", true);
        verify(mockEventListener).onConnectionStateChange(
                new ConnectionStateChange(ConnectionState.CONNECTING, ConnectionState.DISCONNECTED));
    }

    @Test
    public void testOnCloseCallbackDoesNotCallListenerIfItIsNotBoundToDisconnectedEvent() throws URISyntaxException {
        connection = new WebSocketConnection(URL, ACTIVITY_TIMEOUT, PONG_TIMEOUT, PROXY, factory);
        connection.bind(ConnectionState.CONNECTED, mockEventListener);

        connection.connect();
        connection.onClose(1, "reason", true);
        verify(mockEventListener, never()).onConnectionStateChange(any(ConnectionStateChange.class));
    }

    @Test
    public void testOnErrorCallbackRaisesErrorEvent() {
        connection.connect();
        verify(mockEventListener).onConnectionStateChange(
                new ConnectionStateChange(ConnectionState.DISCONNECTED, ConnectionState.CONNECTING));

        final Exception e = new Exception();
        connection.onError(e);
        verify(mockEventListener).onError("An exception was thrown by the websocket", null, e);
    }

    @Test
    public void testDisonnectCallIsDelegatedToUnderlyingConnection() {
        connection.connect();
        connection.onMessage(CONN_ESTABLISHED_EVENT);

        connection.disconnect();
        verify(mockUnderlyingConnection).close();
    }

    @Test
    public void testDisconnectInConnectedStateUpdatesStateToDisconnectingAndNotifiesListener() {
        connection.connect();
        connection.onMessage(CONN_ESTABLISHED_EVENT);

        connection.disconnect();
        verify(mockEventListener).onConnectionStateChange(
                new ConnectionStateChange(ConnectionState.CONNECTED, ConnectionState.DISCONNECTING));
        assertEquals(ConnectionState.DISCONNECTING, connection.getState());
    }

    @Test
    public void testDisconnectInDisconnectedStateIsIgnored() {
        connection.disconnect();

        verify(mockUnderlyingConnection, times(0)).close();
        verify(mockEventListener, times(0)).onConnectionStateChange(any(ConnectionStateChange.class));
    }

    @Test
    public void testDisconnectInConnectingStateIsIgnored() {
        connection.connect();

        connection.disconnect();

        verify(mockUnderlyingConnection, times(0)).close();
        verify(mockEventListener, times(1)).onConnectionStateChange(any(ConnectionStateChange.class));
    }

    @Test
    public void testDisconnectInDisconnectingStateIsIgnored() {
        connection.connect();
        connection.onMessage(CONN_ESTABLISHED_EVENT);

        connection.disconnect();

        verify(mockUnderlyingConnection, times(1)).close();
        verify(mockEventListener, times(3)).onConnectionStateChange(any(ConnectionStateChange.class));
    }

    /* end of tests */

    private void connect() {
        connection.connect();
        connection.onMessage(CONN_ESTABLISHED_EVENT);
    }
}
