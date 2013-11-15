package com.pusher.client.connection.websocket;

import static com.pusher.client.connection.websocket.WebSocketConnection.PING_EVENT_SERIALIZED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.pusher.client.channel.impl.ChannelManager;
import com.pusher.client.connection.websocket.WebSocketConnection.PingCheck;
import com.pusher.client.connection.websocket.WebSocketConnection.PongCheck;
import com.pusher.client.util.Factory;
import com.pusher.client.util.InstantExecutor;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionPingTest {

    private static final long ACTIVITY_TIMEOUT = 120000;
    private static final long PONG_TIMEOUT = 30000;
    private static final String URL = "ws://ws.example.com/";
    private static final String CONN_ESTABLISHED_EVENT =
            "{\"event\":\"pusher:connection_established\",\"data\":\"{\\\"socket_id\\\":\\\"21112.816204\\\"}\"}";

    @Mock
    private ChannelManager mockChannelManager;
    @Mock
    private WebSocketClientWrapper mockUnderlyingConnection;
    @Mock
    private Factory factory;
    @Mock
    private InstantExecutor executor;
    @Mock
    @SuppressWarnings("rawtypes")
    private ScheduledFuture future;

    private long now = System.currentTimeMillis();

    /**
     * Subject under test
     */
    private WebSocketConnection connection;


    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws URISyntaxException, SSLException {
        when(factory.getChannelManager()).thenReturn(mockChannelManager);
        when(factory.newWebSocketClientWrapper(any(URI.class), any(WebSocketConnection.class)))
                .thenReturn(mockUnderlyingConnection);
        when(factory.getEventQueue()).thenReturn(executor);
        when(executor.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(future);
        when(factory.timeNow()).thenReturn(now);

        doCallRealMethod().when(executor).execute(any(Runnable.class));

        this.connection = new WebSocketConnection(URL, ACTIVITY_TIMEOUT, PONG_TIMEOUT, factory);
    }

    @Test
    public void noPingSentOrScheduledIfNotConnectedAtCheckTime() {
        connection.pingCheckNow();

        verify(mockUnderlyingConnection, never()).send(any(String.class));
        verify(executor, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void pingCheckIsScheduledOnConnect() {
        connection.connect();
        connection.onMessage(CONN_ESTABLISHED_EVENT);

        verify(executor).schedule(isA(PingCheck.class), eq(ACTIVITY_TIMEOUT), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void pingCheckRescheduledWhenThereHasBeenActivity() {
        connection.connect();
        connection.onMessage(CONN_ESTABLISHED_EVENT);
        verify(executor).schedule(isA(PingCheck.class), eq(ACTIVITY_TIMEOUT), eq(TimeUnit.MILLISECONDS));

        connection.lastActivity = now - 3000;
        connection.pingCheckNow();

        verify(executor).schedule(isA(PingCheck.class), eq(ACTIVITY_TIMEOUT - 3000), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void pingSentNextCheckScheduledAndPongTimeoutSetIfDue() {
        connection.connect();
        connection.onMessage(CONN_ESTABLISHED_EVENT);
        verify(executor).schedule(isA(PingCheck.class), eq(ACTIVITY_TIMEOUT), eq(TimeUnit.MILLISECONDS));

        connection.lastActivity = now - ACTIVITY_TIMEOUT - 10;
        connection.pingCheckNow();

        verify(mockUnderlyingConnection).send(PING_EVENT_SERIALIZED);
        verify(executor).schedule(isA(PingCheck.class), eq(ACTIVITY_TIMEOUT + 500), eq(TimeUnit.MILLISECONDS));
        verify(executor).schedule(isA(PongCheck.class), eq(PONG_TIMEOUT), eq(TimeUnit.MILLISECONDS));
    }
}
