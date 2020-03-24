package com.pusher.client.channel.impl;

import com.pusher.client.Authorizer;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrivateEncryptedChannelImplTest {

    private String authorizer_valid = "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\",\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=\"}";
    private String authorizer_missingAuthKey = "{\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=\"}";
    private String authorizer_missingSharedSecret = "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\"}";
    private String authorizer_malformedJson = "potatoes";

    private InternalConnection internalConnection;
    private Authorizer authorizer;
    private Factory factory;

    @Before
    public void setup() {
        internalConnection = mock(InternalConnection.class);
        authorizer = mock(Authorizer.class);
        factory = mock(Factory.class);
    }

    @Test
    public void testPrepareThrowsNoExceptionIfValidAuthorizer() {
        when(authorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_valid);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                internalConnection, "private-encrypted-channel", authorizer, factory);

        Exception exception = null;
        try {
            channel.prepareChannel();
        } catch (Exception e) {
            exception = e;
        }

        assertNull(exception);
    }

    @Test
    public void testPrepareThrowsExceptionIfNoAuthKey() {
        when(authorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingAuthKey);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                internalConnection, "private-encrypted-channel", authorizer, factory);

        Exception exception = null;
        try {
            channel.prepareChannel();
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
    }

    @Test
    public void testPrepareThrowsExceptionIfNoSharedSecret() {
        when(authorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingSharedSecret);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                internalConnection, "private-encrypted-channel", authorizer, factory);

        Exception exception = null;
        try {
            channel.prepareChannel();
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
    }

    @Test
    public void testPrepareThrowsExceptionIfMalformedJson() {
        when(authorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_malformedJson);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                internalConnection, "private-encrypted-channel", authorizer, factory);

        Exception exception = null;
        try {
            channel.prepareChannel();
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
    }
}
