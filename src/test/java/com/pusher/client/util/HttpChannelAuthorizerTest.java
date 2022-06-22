package com.pusher.client.util;

import org.junit.Test;

import com.pusher.client.AuthorizationFailureException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpChannelAuthorizerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithMalformedURLThrowsRuntimeException() {
        new HttpChannelAuthorizer("bad url");
    }

    @Test
    public void testHTTPURLIsIdentifiedAsSSL() {
        final HttpChannelAuthorizer auth = new HttpChannelAuthorizer("http://example.com/auth");
        assertFalse(auth.isSSL());
    }

    @Test
    public void testHTTPSURLIsIdentifiedAsSSL() {
        final HttpChannelAuthorizer auth = new HttpChannelAuthorizer("https://example.com/auth");
        assertTrue(auth.isSSL());
    }

    @Test(expected = AuthorizationFailureException.class)
    public void testNon200ResponseThrowsAuthorizationFailureException() {
        final HttpChannelAuthorizer auth = new HttpChannelAuthorizer("https://127.0.0.1/no-way-this-is-a-valid-url");
        auth.authorize("private-fish", "some socket id");
    }
}
