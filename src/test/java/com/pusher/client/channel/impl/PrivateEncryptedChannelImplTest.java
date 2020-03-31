package com.pusher.client.channel.impl;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.ChannelEventListener;
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
    public void checkAuthenticationSucceedsGivenValidAuthorizer() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_valid);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, "private-encrypted-channel", mockAuthorizer, mockedFactory);

        channel.checkAuthentication();
    }

    protected ChannelEventListener getEventListener() {
        return mock(PrivateEncryptedChannelEventListener.class);
    }

    @Test(expected = AuthorizationFailureException.class)
    public void checkAuthenticationThrowsExceptionIfNoAuthKey() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingAuthKey);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, "private-encrypted-channel", mockAuthorizer, mockedFactory);

        channel.checkAuthentication();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void checkAuthenticationThrowsExceptionIfNoSharedSecret() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingSharedSecret);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, "private-encrypted-channel", mockAuthorizer, mockedFactory);

        channel.checkAuthentication();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void checkAuthenticationThrowsExceptionIfMalformedJson() {
        when(mockAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_malformedJson);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockInternalConnection, "private-encrypted-channel", mockAuthorizer, mockedFactory);

        channel.checkAuthentication();
    }

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


    @Override
    @Test
    public void testReturnsCorrectSubscribeMessage() {
        assertEquals("{\"event\":\"pusher:subscribe\",\"data\":{" +
                "\"channel\":\"" + getChannelName() + "\"," +
                "\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\""+
                "}}", channel.toSubscribeMessage());
    }
}
