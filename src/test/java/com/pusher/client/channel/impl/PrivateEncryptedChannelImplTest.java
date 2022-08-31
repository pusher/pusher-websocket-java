package com.pusher.client.channel.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.ChannelAuthorizer;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.crypto.nacl.SecretBoxOpener;
import com.pusher.client.crypto.nacl.SecretBoxOpenerFactory;
import com.pusher.client.util.internal.Base64;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrivateEncryptedChannelImplTest extends ChannelImplTest {

    final String AUTH_RESPONSE =
            "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\",\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=\"}";
    final String AUTH_RESPONSE_MISSING_AUTH =
            "{\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=\"}";
    final String AUTH_RESPONSE_MISSING_SHARED_SECRET =
            "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\"}";
    final String AUTH_RESPONSE_INVALID_JSON = "potatoes";
    final String SHARED_SECRET = "iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=";
    final String AUTH_RESPONSE_INCORRECT_SHARED_SECRET =
            "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\",\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5do0=\"}";
    final String SHARED_SECRET_INCORRECT =
            "iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5do0=";

    @Mock
    InternalConnection mockInternalConnection;

    @Mock
    ChannelAuthorizer mockChannelAuthorizer;

    @Mock
    SecretBoxOpenerFactory mockSecretBoxOpenerFactory;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        when(mockChannelAuthorizer.authorize(eq(getChannelName()), anyString()))
                .thenReturn(AUTH_RESPONSE);
    }

    protected PrivateEncryptedChannelImpl newInstance() {
        return new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockChannelAuthorizer,
                factory,
                mockSecretBoxOpenerFactory
        );
    }

    @Override
    protected ChannelImpl newInstance(final String channelName) {
        return new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                channelName,
                mockChannelAuthorizer,
                factory,
                mockSecretBoxOpenerFactory
        );
    }

    protected String getChannelName() {
        return "private-encrypted-channel";
    }

    @Test
    public void toStringIsAccurate() {
        assertEquals(
                "[Private Encrypted Channel: name=" + getChannelName() + "]",
                channel.toString()
        );
    }

  /*
    TESTING VALID PRIVATE ENCRYPTED CHANNEL NAMES
     */

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void testPublicChannelName() {
        newInstance("stuffchannel");
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void testPresenceChannelName() {
        newInstance("presence-stuffchannel");
    }

    @Override
    @Test
    public void testPrivateEncryptedChannelName() {
        newInstance("private-encrypted-stuffchannel");
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void testPrivateChannelName() {
        newInstance("private-stuffchannel");
    }

  /*
    TESTING SUBSCRIBE MESSAGE
     */

    @Override
    @Test
    public void testReturnsCorrectSubscribeMessage() {
        assertEquals(
                "{\"event\":\"pusher:subscribe\",\"data\":{" +
                        "\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\"," +
                        "\"channel\":\"" +
                        getChannelName() +
                        "\"" +
                        "}}",
                channel.toSubscribeMessage()
        );
    }

  /*
    TESTING AUTHENTICATION METHOD
     */

    @Test
    public void authenticationSucceedsGivenValidChannelAuthorizer() {
        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE);

        PrivateEncryptedChannelImpl channel = newInstance();

        channel.toSubscribeMessage();
    }

    protected ChannelEventListener getEventListener() {
        return mock(PrivateEncryptedChannelEventListener.class);
    }

    @Test(expected = AuthorizationFailureException.class)
    public void authenticationThrowsExceptionIfNoAuthKey() {
        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE_MISSING_AUTH);

        PrivateEncryptedChannelImpl channel = newInstance();

        channel.toSubscribeMessage();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void authenticationThrowsExceptionIfNoSharedSecret() {
        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE_MISSING_SHARED_SECRET);

        PrivateEncryptedChannelImpl channel = newInstance();

        channel.toSubscribeMessage();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void authenticationThrowsExceptionIfMalformedJson() {
        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE_INVALID_JSON);

        PrivateEncryptedChannelImpl channel = newInstance();

        channel.toSubscribeMessage();
    }

    /*
      ON MESSAGE
       */
    @Test
    public void testDataIsExtractedFromMessageAndPassedToSingleListener() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockChannelAuthorizer,
                factory,
                mockSecretBoxOpenerFactory
        );

        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener = mock(
                PrivateEncryptedChannelEventListener.class
        );

        channel.bind("event1", mockListener);
        channel.handleEvent(
                PusherEvent.fromJson(
                        "{\"event\":\"event1\",\"data\":\"{" +
                                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                                "}\"}"
                )
        );

        verify(mockListener, times(1)).onEvent(argCaptor.capture());
        assertEquals("event1", argCaptor.getValue().getEventName());
        assertEquals(
                "{\"message\":\"hello world\"}",
                argCaptor.getValue().getData()
        );
    }

    @Test
    public void testDataIsExtractedFromMessageAndPassedToSingleListenerGlobalEvent() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockChannelAuthorizer,
                factory,
                mockSecretBoxOpenerFactory
        );

        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener = mock(
                PrivateEncryptedChannelEventListener.class
        );

        channel.bindGlobal(mockListener);

        channel.handleEvent(
                PusherEvent.fromJson(
                        "{\"event\":\"event1\",\"data\":\"{" +
                                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                                "}\"}"
                )
        );

        verify(mockListener, times(1)).onEvent(argCaptor.capture());
        assertEquals("event1", argCaptor.getValue().getEventName());
        assertEquals(
                "{\"message\":\"hello world\"}",
                argCaptor.getValue().getData()
        );
    }

    @Test
    public void testDataIsExtractedFromMessageAndPassedToMultipleListeners() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockChannelAuthorizer,
                factory,
                mockSecretBoxOpenerFactory
        );

        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener1 = mock(
                PrivateEncryptedChannelEventListener.class
        );
        PrivateEncryptedChannelEventListener mockListener2 = mock(
                PrivateEncryptedChannelEventListener.class
        );

        channel.bind("event1", mockListener1);
        channel.bind("event1", mockListener2);
        channel.handleEvent(
                PusherEvent.fromJson(
                        "{\"event\":\"event1\",\"data\":\"{" +
                                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                                "}\"}"
                )
        );

        verify(mockListener1).onEvent(argCaptor.capture());
        assertEquals("event1", argCaptor.getValue().getEventName());
        assertEquals(
                "{\"message\":\"hello world\"}",
                argCaptor.getValue().getData()
        );

        verify(mockListener2).onEvent(argCaptor.capture());
        assertEquals("event1", argCaptor.getValue().getEventName());
        assertEquals(
                "{\"message\":\"hello world\"}",
                argCaptor.getValue().getData()
        );
    }

    @Test
    public void handleEventRaisesExceptionWhenFailingToDecryptTwice() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockChannelAuthorizer,
                factory,
                mockSecretBoxOpenerFactory
        );

        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE_INCORRECT_SHARED_SECRET)
                .thenReturn(AUTH_RESPONSE_INCORRECT_SHARED_SECRET);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET_INCORRECT)))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET_INCORRECT)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener1 = mock(
                PrivateEncryptedChannelEventListener.class
        );
        channel.bind("event1", mockListener1);
        channel.handleEvent(
                PusherEvent.fromJson(
                        "{\"event\":\"event1\",\"data\":\"{" +
                                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                                "}\"}"
                )
        );

        verify(mockListener1).onDecryptionFailure(anyString(), anyString());
    }

    @Test
    public void handleEventRetriesDecryptionOnce() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockChannelAuthorizer,
                factory,
                mockSecretBoxOpenerFactory
        );

        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE_INCORRECT_SHARED_SECRET)
                .thenReturn(AUTH_RESPONSE);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET_INCORRECT)))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener1 = mock(
                PrivateEncryptedChannelEventListener.class
        );
        channel.bind("event1", mockListener1);
        channel.handleEvent(
                PusherEvent.fromJson(
                        "{\"event\":\"event1\",\"data\":\"{" +
                                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                                "}\"}"
                )
        );

        verify(mockListener1).onEvent(argCaptor.capture());
        assertEquals("event1", argCaptor.getValue().getEventName());
        assertEquals(
                "{\"message\":\"hello world\"}",
                argCaptor.getValue().getData()
        );
    }

    @Test
    public void twoEventsReceivedWithSecondRetryCorrect() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockChannelAuthorizer,
                factory,
                mockSecretBoxOpenerFactory
        );

        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE_INCORRECT_SHARED_SECRET)
                .thenReturn(AUTH_RESPONSE_INCORRECT_SHARED_SECRET)
                .thenReturn(AUTH_RESPONSE);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET_INCORRECT)))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET_INCORRECT)))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener1 = mock(
                PrivateEncryptedChannelEventListener.class
        );
        channel.bind("event1", mockListener1);
        channel.handleEvent(
                PusherEvent.fromJson(
                        "{\"event\":\"event1\",\"data\":\"{" +
                                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                                "}\"}"
                )
        );

        verify(mockListener1)
                .onDecryptionFailure("event1", "Failed to decrypt message.");

        // send a second message
        channel.handleEvent(
                PusherEvent.fromJson(
                        "{\"event\":\"event1\",\"data\":\"{" +
                                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                                "}\"}"
                )
        );

        verify(mockListener1).onEvent(argCaptor.capture());
        assertEquals("event1", argCaptor.getValue().getEventName());
        assertEquals(
                "{\"message\":\"hello world\"}",
                argCaptor.getValue().getData()
        );
    }

    @Test
    public void twoEventsReceivedWithIncorrectSharedSecret() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockChannelAuthorizer,
                factory,
                mockSecretBoxOpenerFactory
        );

        when(
                mockChannelAuthorizer.authorize(
                        Matchers.anyString(),
                        Matchers.anyString()
                )
        )
                .thenReturn(AUTH_RESPONSE_INCORRECT_SHARED_SECRET)
                .thenReturn(AUTH_RESPONSE_INCORRECT_SHARED_SECRET)
                .thenReturn(AUTH_RESPONSE_INCORRECT_SHARED_SECRET);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET_INCORRECT)))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET_INCORRECT)))
                .thenReturn(new SecretBoxOpener(Base64.decode(SHARED_SECRET_INCORRECT)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener1 = mock(
                PrivateEncryptedChannelEventListener.class
        );
        channel.bind("event1", mockListener1);
        channel.handleEvent(
                PusherEvent.fromJson(
                        "{\"event\":\"event1\",\"data\":\"{" +
                                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                                "}\"}"
                )
        );
        // send a second message
        channel.handleEvent(
                PusherEvent.fromJson(
                        "{\"event\":\"event1\",\"data\":\"{" +
                                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                                "}\"}"
                )
        );

        verify(mockListener1, times(2))
                .onDecryptionFailure("event1", "Failed to decrypt message.");
    }
}
