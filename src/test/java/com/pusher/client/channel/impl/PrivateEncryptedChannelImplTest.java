package com.pusher.client.channel.impl;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrivateEncryptedChannelImplTest extends ChannelImplTest {

    String authorizer_valid = "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\",\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=\"}";
    String authorizer_missingAuthKey = "{\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=\"}";
    String authorizer_missingSharedSecret = "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\"}";
    String authorizer_malformedJson = "potatoes";


    @Mock
    InternalConnection mockInternalConnection;
    @Mock
    Authorizer mockAuthorizer;
    @Mock
    Factory mockedFactory;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        when(mockAuthorizer.authorize(eq(getChannelName()), anyString())).thenReturn(authorizer_valid);
    }

    @Override
    protected ChannelImpl newInstance(final String channelName) {
        return new PrivateEncryptedChannelImpl(mockInternalConnection, channelName, mockAuthorizer, factory);
    }

    protected String getChannelName() {
        return "private-encrypted-channel";
    }

    @Test
    public void toStringIsAccurate() {
        assertEquals("[Private Encrypted Channel: name="+getChannelName()+"]", channel.toString());
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
    public void testPrivateChannelName() {  newInstance("private-stuffchannel");  }

    /*
    TESTING SUBSCRIBE MESSAGE
     */

    @Override
    @Test
    public void testReturnsCorrectSubscribeMessage() {
        assertEquals("{\"event\":\"pusher:subscribe\",\"data\":{" +
                "\"channel\":\"" + getChannelName() + "\"," +
                "\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\""+
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
                mockInternalConnection, getChannelName(), mockAuthorizer, mockedFactory);

        channel.toSubscribeMessage();
    }

    protected ChannelEventListener getEventListener() {
        return mock(PrivateEncryptedChannelEventListener.class);
    }

    @Test(expected = AuthorizationFailureException.class)
    public void authenticationThrowsExceptionIfNoAuthKey() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingAuthKey);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, getChannelName(), mockAuthorizer, mockedFactory);

        channel.toSubscribeMessage();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void authenticationThrowsExceptionIfNoSharedSecret() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingSharedSecret);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, getChannelName(), mockAuthorizer, mockedFactory);

        channel.toSubscribeMessage();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void authenticationThrowsExceptionIfMalformedJson() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_malformedJson);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, getChannelName(), mockAuthorizer, mockedFactory);

        channel.toSubscribeMessage();
    }

    /*
    TESTING SECRET BOX
     */

    @Test
    public void secretBoxOpenerIsCleared() {
        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, getChannelName(), mockAuthorizer, mockedFactory);

        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_valid);

        channel.toSubscribeMessage();
        assertNotNull(channel.secretBoxOpener);

        channel.updateState(ChannelState.UNSUBSCRIBED);
        assertNull(channel.secretBoxOpener);
    }
}