package com.pusher.client.channel.impl;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrivateEncryptedChannelImplTest {

    String authorizer_valid = "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\",\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=\"}";
    String authorizer_missingAuthKey = "{\"shared_secret\":\"iBvNoPVYwByqSfg6anjPpEQ2j051b3rt1Vmnb+z5doo=\"}";
    String authorizer_missingSharedSecret = "{\"auth\":\"636a81ba7e7b15725c00:3ee04892514e8a669dc5d30267221f16727596688894712cad305986e6fc0f3c\"}";
    String authorizer_malformedJson = "potatoes";

    @Mock
    InternalConnection mockedInternalConnection;
    @Mock
    Authorizer mockedAuthorizer;
    @Mock
    Factory mockedFactory;

    @Test
    public void checkAuthenticationSucceedsGivenValidAuthorizer() {
        when(mockedAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_valid);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockedInternalConnection, "private-encrypted-channel", mockedAuthorizer, mockedFactory);

        channel.checkAuthentication();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void checkAuthenticationThrowsExceptionIfNoAuthKey() {
        when(mockedAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingAuthKey);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockedInternalConnection, "private-encrypted-channel", mockedAuthorizer, mockedFactory);

        channel.checkAuthentication();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void checkAuthenticationThrowsExceptionIfNoSharedSecret() {
        when(mockedAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingSharedSecret);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockedInternalConnection, "private-encrypted-channel", mockedAuthorizer, mockedFactory);

        channel.checkAuthentication();
    }

    @Test(expected = AuthorizationFailureException.class)
    public void checkAuthenticationThrowsExceptionIfMalformedJson() {
        when(mockedAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_malformedJson);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockedInternalConnection, "private-encrypted-channel", mockedAuthorizer, mockedFactory);

        channel.checkAuthentication();
    }
}
