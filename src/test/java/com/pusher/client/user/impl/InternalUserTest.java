package com.pusher.client.user.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.pusher.client.UserAuthenticator;
import com.pusher.client.AuthenticationFailureException;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.channel.impl.ChannelManager;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.util.Factory;

@RunWith(MockitoJUnitRunner.class)
public class InternalUserTest {
    private static final String socketId = "123";
    private static final String authenticationResponse = "{\"auth\": \"123:456\", \"user_data\":\"{\\\"id\\\": \\\"someid\\\"}\"}";
    private static final String authenticationResponseMalformed = "{}";
    private static final String signinSuccessEvent = "{\"event\": \"pusher:signin_success\", \"data\": \"{\\\"user_data\\\": \\\"{\\\\\\\"id\\\\\\\":\\\\\\\"1\\\\\\\"}\\\"}\"}";
    private static final String signinSuccessEventMissingId = "{\"event\": \"pusher:signin_success\", \"data\": \"{\\\"user_data\\\": \\\"{}\\\"}\"}";
    private static final String signinSuccessEventMalformed = "{\"event\": \"pusher:signin_success\", \"data\": \"{}\"}";

    private InternalUser user;
    private @Mock UserAuthenticator mockUserAuthenticator;
    private @Mock InternalConnection mockConnection;
    private @Mock ChannelManager mockChannelManager;
    private @Mock Factory mockFactory;
    private @Mock SubscriptionEventListener mockEventListener;

    @Before
    public void setUp() {
        when(mockConnection.getSocketId()).thenReturn(socketId);
        when(mockFactory.getChannelManager()).thenReturn(mockChannelManager);
        user = new InternalUser(mockConnection, mockUserAuthenticator, mockFactory);
    }

    @Test
    public void testSigninWhenNotConnected() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);
        user.signin();
        verify(mockUserAuthenticator, never()).authenticate(any(String.class));
        verify(mockConnection, never()).sendMessage(any(String.class));
    }

    @Test
    public void testSigninWhenConnected() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        when(mockUserAuthenticator.authenticate(socketId)).thenReturn(authenticationResponse);
        user.signin();
        verify(mockConnection).sendMessage(any(String.class));
    }

    @Test(expected = AuthenticationFailureException.class)
    public void testSigninMalformedResponse() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        when(mockUserAuthenticator.authenticate(socketId)).thenReturn(authenticationResponseMalformed);
        user.signin();
    }

    @Test
    public void testHandleEventSigninSuccessEvent() {
        user.handleEvent("pusher:signin_success", signinSuccessEvent);
        assertEquals(user.userId(), "1");
        verify(mockChannelManager).subscribeTo(any(ServerToUserChannel.class), eq(null));
    }

    @Test
    public void testHandleEventSigninSuccessEventMissingId() {
        user.handleEvent("pusher:signin_success", signinSuccessEventMissingId);
        assertNull(user.userId());
        verify(mockChannelManager, never()).subscribeTo(any(ServerToUserChannel.class), eq(null));
    }

    @Test
    public void testHandleEventSigninSuccessEventMalformed() {
        user.handleEvent("pusher:signin_success", signinSuccessEventMalformed);
        assertNull(user.userId());
        verify(mockChannelManager, never()).subscribeTo(any(ServerToUserChannel.class), eq(null));
    }

    @Test
    public void testSigninWhenSignedIn() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        user.handleEvent("pusher:signin_success", signinSuccessEvent);
        assertEquals(user.userId(), "1");
        user.signin();
        verify(mockUserAuthenticator, never()).authenticate(any(String.class));
        verify(mockConnection, never()).sendMessage(any(String.class));
    }
}
