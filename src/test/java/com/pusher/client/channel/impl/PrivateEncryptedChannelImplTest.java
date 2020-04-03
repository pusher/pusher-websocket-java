package com.pusher.client.channel.impl;

import com.google.gson.JsonSyntaxException;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.crypto.nacl.SecretBoxOpener;
import com.pusher.client.crypto.nacl.SecretBoxOpenerFactory;
import com.pusher.client.util.Factory;
import com.pusher.client.util.internal.Base64;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrivateEncryptedChannelImplTest extends ChannelImplTest {

    String authorizer_valid = "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\",\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=\"}";
    String valid_sharedSecret = "iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=";
    String authorizer_missingAuthKey = "{\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=\"}";
    String authorizer_missingSharedSecret = "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\"}";
    String authorizer_malformedJson = "potatoes";


    @Mock
    InternalConnection mockInternalConnection;
    @Mock
    Authorizer mockAuthorizer;
    @Mock
    Factory mockFactory;
    @Mock
    SecretBoxOpenerFactory mockSecretBoxOpenerFactory;

    @Mock
    SecretBoxOpener mockSecretBoxOpener;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        when(mockAuthorizer.authorize(eq(getChannelName()), anyString())).thenReturn(authorizer_valid);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Runnable r = (Runnable) invocation.getArguments()[0];
                r.run();
                return null;
            }
        }).when(mockFactory).queueOnEventThread(any(Runnable.class));

    }

    @Override
    protected ChannelImpl newInstance(final String channelName) {
        return new PrivateEncryptedChannelImpl(mockInternalConnection, channelName, mockAuthorizer,
                mockFactory, mockSecretBoxOpenerFactory);
    }

    protected String getChannelName() {
        return "private-encrypted-channel";
    }

    @Test
    public void toStringIsAccurate() {
        assertEquals("[Private Encrypted Channel: name=" + getChannelName() + "]", channel.toString());
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
        assertEquals("{\"event\":\"pusher:subscribe\",\"data\":{" +
                "\"channel\":\"" + getChannelName() + "\"," +
                "\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\"" +
                "}}", channel.toSubscribeMessage());
    }

    /*
    TESTING AUTHENTICATION METHOD
     */

    @Test
    public void authenticationSucceedsGivenValidAuthorizer() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_valid);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, getChannelName(), mockAuthorizer, mockFactory,
                mockSecretBoxOpenerFactory);

        channel.toSubscribeMessage();
    }

    protected PrivateEncryptedChannelEventListener getEventListener() {
        return mock(PrivateEncryptedChannelEventListener.class);
    }

    @Test(expected = AuthorizationFailureException.class)
    public void authenticationThrowsExceptionIfNoAuthKey() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingAuthKey);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, getChannelName(), mockAuthorizer, mockFactory,
                mockSecretBoxOpenerFactory);

        channel.toSubscribeMessage();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void authenticationThrowsExceptionIfNoSharedSecret() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingSharedSecret);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, getChannelName(), mockAuthorizer, mockFactory,
                mockSecretBoxOpenerFactory);

        channel.toSubscribeMessage();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void authenticationThrowsExceptionIfMalformedJson() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_malformedJson);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, getChannelName(), mockAuthorizer, mockFactory,
                mockSecretBoxOpenerFactory);

        channel.toSubscribeMessage();
    }

    /*
    TESTING SECRET BOX
     */

    @Test
    public void secretBoxOpenerIsCleared() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, getChannelName(), mockAuthorizer, mockFactory,
                mockSecretBoxOpenerFactory);

        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_valid);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(mockSecretBoxOpener);

        channel.toSubscribeMessage();

        channel.updateState(ChannelState.UNSUBSCRIBED);
        verify(mockSecretBoxOpener).clearKey();
    }

    /*
    ON MESSAGE
     */
    @Test
    public void testDataIsExtractedFromMessageAndPassedToSingleListener() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockAuthorizer,
                mockFactory,
                mockSecretBoxOpenerFactory);

        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_valid);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(valid_sharedSecret)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener = mock(PrivateEncryptedChannelEventListener.class);

        channel.bind("my-event", mockListener);
        channel.onMessage("my-event", "{\"event\":\"event1\",\"data\":\"{" +
                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                "}\"}");

        verify(mockListener, times(1)).onEvent(argCaptor.capture());
        assertEquals("event1", argCaptor.getValue().getEventName());
        assertEquals("{\"message\":\"hello world\"}", argCaptor.getValue().getData());
    }

    @Test
    public void testDataIsExtractedFromMessageAndPassedToMultipleListeners() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockAuthorizer,
                mockFactory,
                mockSecretBoxOpenerFactory);

        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_valid);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(valid_sharedSecret)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener1 = mock(PrivateEncryptedChannelEventListener.class);
        PrivateEncryptedChannelEventListener mockListener2 = mock(PrivateEncryptedChannelEventListener.class);

        channel.bind("my-event", mockListener1);
        channel.bind("my-event", mockListener2);
        channel.onMessage("my-event", "{\"event\":\"event1\",\"data\":\"{" +
                "\\\"nonce\\\": \\\"4sVYwy4j/8dCcjyxtPCWyk19GaaViaW9\\\"," +
                "\\\"ciphertext\\\": \\\"/GMESnFGlbNn01BuBjp31XYa3i9vZsGKR8fgR9EDhXKx3lzGiUD501A=\\\"" +
                "}\"}");

        verify(mockListener1).onEvent(argCaptor.capture());
        assertEquals("event1", argCaptor.getValue().getEventName());
        assertEquals("{\"message\":\"hello world\"}", argCaptor.getValue().getData());

        verify(mockListener2).onEvent(argCaptor.capture());
        assertEquals("event1", argCaptor.getValue().getEventName());
        assertEquals("{\"message\":\"hello world\"}", argCaptor.getValue().getData());
    }

    @Captor
    ArgumentCaptor<Exception> exceptionArgumentCaptor;

    @Test
    public void onMessageHandlesNoNonceOrCypherText() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockAuthorizer,
                mockFactory,
                mockSecretBoxOpenerFactory);

        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_valid);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(valid_sharedSecret)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener = mock(PrivateEncryptedChannelEventListener.class);

        channel.bind("my-event", mockListener);
        channel.onMessage("my-event", "{\"event\":\"event1\",\"data\":\"{}\"}");

        verify(mockListener, times(1)).onDecryptionFailure(
                exceptionArgumentCaptor.capture());
        assertEquals(exceptionArgumentCaptor.getValue().getClass(), NullPointerException.class);
    }

    @Test
    public void onMessageHandlesInvalidJson() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection,
                getChannelName(),
                mockAuthorizer,
                mockFactory,
                mockSecretBoxOpenerFactory);

        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_valid);
        when(mockSecretBoxOpenerFactory.create(any()))
                .thenReturn(new SecretBoxOpener(Base64.decode(valid_sharedSecret)));

        channel.toSubscribeMessage();

        PrivateEncryptedChannelEventListener mockListener = mock(PrivateEncryptedChannelEventListener.class);

        channel.bind("my-event", mockListener);
        channel.onMessage("my-event", "{\"event\":\"event1\",\"data\":\"{potatoes}\"}");

        verify(mockListener, times(1)).onDecryptionFailure(
                exceptionArgumentCaptor.capture());
        assertEquals(exceptionArgumentCaptor.getValue().getClass(), JsonSyntaxException.class);
    }

}
