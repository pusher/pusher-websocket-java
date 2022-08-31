package com.pusher.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.impl.ChannelImpl;
import com.pusher.client.channel.impl.ChannelManager;
import com.pusher.client.channel.impl.PresenceChannelImpl;
import com.pusher.client.channel.impl.PrivateChannelImpl;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.user.impl.InternalUser;
import com.pusher.client.util.Factory;
import com.pusher.client.util.HttpChannelAuthorizer;
import com.pusher.client.util.HttpUserAuthenticator;
import com.pusher.client.util.PusherEventHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class PusherTest {

    private static final String API_KEY = "123456";
    private static final String PUBLIC_CHANNEL_NAME = "my-channel";
    private static final String PRIVATE_CHANNEL_NAME = "private-my-channel";
    private static final String PRESENCE_CHANNEL_NAME = "presence-my-channel";

    private Pusher pusher;
    private PusherOptions options;
    private ChannelAuthorizer channelAuthorizer;

    @Mock
    private InternalUser mockUser;

    @Mock
    private InternalConnection mockConnection;

    @Mock
    private ChannelManager mockChannelManager;

    @Mock
    private PusherEventHandler mockEventHandler;

    @Mock
    private ConnectionEventListener mockConnectionEventListener;

    @Mock
    private ChannelImpl mockPublicChannel;

    @Mock
    private PrivateChannelImpl mockPrivateChannel;

    @Mock
    private PresenceChannelImpl mockPresenceChannel;

    @Mock
    private ChannelEventListener mockChannelEventListener;

    @Mock
    private PrivateChannelEventListener mockPrivateChannelEventListener;

    @Mock
    private PresenceChannelEventListener mockPresenceChannelEventListener;

    @Mock
    private Factory factory;

    @Before
    public void setUp() {
        channelAuthorizer = new HttpChannelAuthorizer("http://www.example.com");
        options =
                new PusherOptions()
                        .setChannelAuthorizer(channelAuthorizer)
                        .setUserAuthenticator(
                                new HttpUserAuthenticator("http://user-auth.com")
                        );

        when(factory.getConnection(eq(API_KEY), any(PusherOptions.class), any(PusherEventHandler.class)))
                .thenReturn(mockConnection);
        when(factory.getChannelManager()).thenReturn(mockChannelManager);
        when(factory.newPublicChannel(PUBLIC_CHANNEL_NAME))
                .thenReturn(mockPublicChannel);
        when(
                factory.newPrivateChannel(
                        mockConnection,
                        PRIVATE_CHANNEL_NAME,
                        channelAuthorizer
                )
        )
                .thenReturn(mockPrivateChannel);
        when(
                factory.newPresenceChannel(
                        mockConnection,
                        PRESENCE_CHANNEL_NAME,
                        channelAuthorizer
                )
        )
                .thenReturn(mockPresenceChannel);
        when(factory.newUser(eq(mockConnection), any(UserAuthenticator.class)))
                .thenReturn(mockUser);
        doAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) {
                        final Runnable r = (Runnable) invocation.getArguments()[0];
                        r.run();
                        return null;
                    }
                }
        )
                .when(factory)
                .queueOnEventThread(any(Runnable.class));
        pusher = new Pusher(API_KEY, options, factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAPIKeyThrowsIllegalArgumentException() {
        new Pusher(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyAPIKeyThrowsIllegalArgumentException() {
        new Pusher("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPusherOptionsThrowsIllegalArgumentException() {
        new Pusher(API_KEY, null);
    }

    @Test
    public void testCreatesConnectionObjectWhenConstructed() {
        assertNotNull(pusher.getConnection());
        assertSame(mockConnection, pusher.getConnection());
    }

    @Test
    public void testConnectCallWithNoListenerIsDelegatedToUnderlyingConnection() {
        pusher.connect();
        verify(mockConnection).connect();
    }

    @Test
    public void testDisconnectCallIsDelegatedToUnderlyingConnection() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);

        pusher.disconnect();
        verify(mockConnection).disconnect();
    }

    @Test
    public void testDisconnectCallDoesNothingIfStateIsDisconnected() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);

        pusher.disconnect();
        verify(mockConnection, never()).disconnect();
    }

    @Test
    public void testDisconnectCallDoesNothingIfStateIsDisconnecting() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTING);

        pusher.disconnect();
        verify(mockConnection, never()).disconnect();
    }

    @Test
    public void testConnectCallWithListenerAndEventsBindsListenerToEventsBeforeConnecting() {
        pusher.connect(
                mockConnectionEventListener,
                ConnectionState.CONNECTED,
                ConnectionState.DISCONNECTED
        );

        verify(mockConnection)
                .bind(ConnectionState.CONNECTED, mockConnectionEventListener);
        verify(mockConnection)
                .bind(ConnectionState.DISCONNECTED, mockConnectionEventListener);
        verify(mockConnection).connect();
    }

    @Test
    public void testConnectCallWithListenerAndNoEventsBindsListenerToAllEventsBeforeConnecting() {
        pusher.connect(mockConnectionEventListener);

        verify(mockConnection)
                .bind(ConnectionState.ALL, mockConnectionEventListener);
        verify(mockConnection).connect();
    }

    @Test
    public void testConnectCallWithNullListenerAndNoEventsJustConnectsWithoutBinding() {
        pusher.connect(null);

        verify(mockConnection, never())
                .bind(any(ConnectionState.class), any(ConnectionEventListener.class));
        verify(mockConnection).connect();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectCallWithNullListenerAndEventsThrowsException() {
        pusher.connect(null, ConnectionState.CONNECTED);
    }

    @Test
    public void testSubscribeWithoutListenerCreatesPublicChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        pusher.subscribe(PUBLIC_CHANNEL_NAME);

        verify(mockChannelManager).subscribeTo(mockPublicChannel, null);
    }

    @Test
    public void testSubscribeCreatesPublicChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        pusher.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener);

        verify(mockChannelManager)
                .subscribeTo(mockPublicChannel, mockChannelEventListener);
    }

    @Test
    public void testSubscribeWithEventNamesCreatesPublicChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        pusher.subscribe(
                PUBLIC_CHANNEL_NAME,
                mockChannelEventListener,
                "event1",
                "event2"
        );

        verify(mockChannelManager)
                .subscribeTo(
                        mockPublicChannel,
                        mockChannelEventListener,
                        "event1",
                        "event2"
                );
    }

    @Test
    public void testSubscribeWhenConnectingCreatesPublicChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTING);

        pusher.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener);

        verify(mockChannelManager)
                .subscribeTo(mockPublicChannel, mockChannelEventListener);
    }

    @Test
    public void testSubscribeWhenDisconnectedCreatesPublicChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);

        pusher.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener);

        verify(mockChannelManager)
                .subscribeTo(mockPublicChannel, mockChannelEventListener);
    }

    @Test
    public void testSubscribePresenceCreatesPresenceChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        pusher.subscribePresence(
                PRESENCE_CHANNEL_NAME,
                mockPresenceChannelEventListener
        );

        verify(mockChannelManager)
                .subscribeTo(mockPresenceChannel, mockPresenceChannelEventListener);
    }

    @Test
    public void testSubscribePresenceWithEventNamesCreatesPresenceChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        pusher.subscribePresence(
                PRESENCE_CHANNEL_NAME,
                mockPresenceChannelEventListener,
                "event1",
                "event2"
        );

        verify(mockChannelManager)
                .subscribeTo(
                        mockPresenceChannel,
                        mockPresenceChannelEventListener,
                        "event1",
                        "event2"
                );
    }

    @Test(expected = IllegalStateException.class)
    public void testSubscribePresenceIfNoPusherOptionsHaveBeenPassedThrowsException() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        pusher = new Pusher(API_KEY);

        pusher.subscribePresence(
                PRESENCE_CHANNEL_NAME,
                mockPresenceChannelEventListener
        );
    }

    @Test(expected = IllegalStateException.class)
    public void testSubscribePresenceIfPusherOptionsHaveBeenPassedButNoChannelAuthorizerHasBeenSetThrowsException() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        options.setChannelAuthorizer(null);
        pusher = new Pusher(API_KEY, options);

        pusher.subscribePresence(
                PRESENCE_CHANNEL_NAME,
                mockPresenceChannelEventListener
        );
    }

    @Test
    public void testSubscribePresenceWhenConnectingCreatesPresenceChannelAndDelegatesCallToChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTING);

        pusher.subscribePresence(
                PRESENCE_CHANNEL_NAME,
                mockPresenceChannelEventListener
        );

        verify(mockChannelManager)
                .subscribeTo(mockPresenceChannel, mockPresenceChannelEventListener);
    }

    @Test
    public void testSubscribePresenceWhenDisconnectedCreatesPresenceChannelAndDelegatesCallToChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);

        pusher.subscribePresence(
                PRESENCE_CHANNEL_NAME,
                mockPresenceChannelEventListener
        );

        verify(mockChannelManager)
                .subscribeTo(mockPresenceChannel, mockPresenceChannelEventListener);
    }

    @Test
    public void testSubscribePrivateCreatesPrivateChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        pusher.subscribePrivate(
                PRIVATE_CHANNEL_NAME,
                mockPrivateChannelEventListener
        );

        verify(mockChannelManager)
                .subscribeTo(mockPrivateChannel, mockPrivateChannelEventListener);
    }

    @Test
    public void testSubscribePrivateWithEventNamesCreatesPrivateChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        pusher.subscribePrivate(
                PRIVATE_CHANNEL_NAME,
                mockPrivateChannelEventListener,
                "event1",
                "event2"
        );

        verify(mockChannelManager)
                .subscribeTo(
                        mockPrivateChannel,
                        mockPrivateChannelEventListener,
                        "event1",
                        "event2"
                );
    }

    @Test(expected = IllegalStateException.class)
    public void testSubscribePrivateIfNoPusherOptionsHaveBeenPassedThrowsException() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        pusher = new Pusher(API_KEY);

        pusher.subscribePrivate(
                PRIVATE_CHANNEL_NAME,
                mockPrivateChannelEventListener
        );
    }

    @Test(expected = IllegalStateException.class)
    public void testSubscribePrivateIfPusherOptionsHaveBeenPassedButNoChannelAuthorizerHasBeenSetThrowsException() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        options.setChannelAuthorizer(null);
        pusher = new Pusher(API_KEY, options);

        pusher.subscribePrivate(
                PRIVATE_CHANNEL_NAME,
                mockPrivateChannelEventListener
        );
    }

    @Test
    public void testSubscribePrivateWhenConnectingCreatesPrivateChannelAndDelegatesCallToChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTING);

        pusher.subscribePrivate(
                PRIVATE_CHANNEL_NAME,
                mockPrivateChannelEventListener
        );

        verify(mockChannelManager)
                .subscribeTo(mockPrivateChannel, mockPrivateChannelEventListener);
    }

    @Test
    public void testSubscribePrivateWhenDisconnectedCreatesPrivateChannelAndDelegatesCallToChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);

        pusher.subscribePrivate(
                PRIVATE_CHANNEL_NAME,
                mockPrivateChannelEventListener
        );

        verify(mockChannelManager)
                .subscribeTo(mockPrivateChannel, mockPrivateChannelEventListener);
    }

    @Test
    public void testUnsubscribeDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        pusher.unsubscribe(PUBLIC_CHANNEL_NAME);
        verify(mockChannelManager).unsubscribeFrom(PUBLIC_CHANNEL_NAME);
    }

    @Test(expected = IllegalStateException.class)
    public void testSigninThrowsIfUserAuthenticatorIsNotSet() {
        options.setUserAuthenticator(null);
        pusher = new Pusher(API_KEY, options);
        pusher.signin();
    }

    @Test
    public void testSigninCallsUserSignin() {
        doNothing().when(mockUser).signin();
        pusher.signin();
        verify(mockUser).signin();
    }
}
