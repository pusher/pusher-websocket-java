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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

    @Test
    public void checkAuthenticationThrowsExceptionIfNoAuthKey() {
        when(mockedAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingAuthKey);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockedInternalConnection, "private-encrypted-channel", mockedAuthorizer, mockedFactory);

        Exception exception = null;
        try {
            channel.checkAuthentication();
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertThat(exception).isInstanceOf(AuthorizationFailureException.class);
        assertThat(exception.getMessage()).isEqualTo(
                "Didn't receive all the fields we expected from the Authorizer, " +
                        "expected an auth token and shared_secret but got: "
                        + authorizer_missingAuthKey);
    }

    @Test
    public void checkAuthenticationThrowsExceptionIfNoSharedSecret() {
        when(mockedAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_missingSharedSecret);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockedInternalConnection, "private-encrypted-channel", mockedAuthorizer, mockedFactory);

        Exception exception = null;
        try {
            channel.checkAuthentication();
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertThat(exception).isInstanceOf(AuthorizationFailureException.class);
        assertThat(exception.getMessage()).isEqualTo("Didn't receive all the fields we " +
                "expected from the Authorizer, expected an auth token and shared_secret but got: "
                + authorizer_missingSharedSecret);
    }

    @Test
    public void checkAuthenticationThrowsExceptionIfMalformedJson() {
        when(mockedAuthorizer.authorize(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(authorizer_malformedJson);

        PrivateEncryptedChannelImpl channel = new PrivateEncryptedChannelImpl(
                mockedInternalConnection, "private-encrypted-channel", mockedAuthorizer, mockedFactory);

        Exception exception = null;
        try {
            channel.checkAuthentication();
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertThat(exception).isInstanceOf(AuthorizationFailureException.class);
        assertThat(exception.getMessage()).isEqualTo(
                "Unable to parse response from Authorizer: " + authorizer_malformedJson);
    }
}
