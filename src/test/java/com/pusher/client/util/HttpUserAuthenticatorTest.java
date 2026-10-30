package com.pusher.client.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pusher.client.AuthenticationFailureException;

import com.sun.net.httpserver.HttpServer;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HttpUserAuthenticatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithMalformedURLThrowsRuntimeException() {
        new HttpUserAuthenticator("bad url");
    }

    @Test
    public void testHTTPURLIsIdentifiedAsSSL() {
        final HttpUserAuthenticator auth = new HttpUserAuthenticator("http://example.com/auth");
        assertFalse(auth.isSSL());
    }

    @Test
    public void testHTTPSURLIsIdentifiedAsSSL() {
        final HttpUserAuthenticator auth = new HttpUserAuthenticator("https://example.com/auth");
        assertTrue(auth.isSSL());
    }

    @Test(expected = AuthenticationFailureException.class)
    public void testNon200ResponseThrowsAuthenticationFailtureException() {
        final HttpUserAuthenticator auth = new HttpUserAuthenticator("https://127.0.0.1/no-way-this-is-a-valid-url");
        auth.authenticate("some socket id");
    }

    @Test
    public void testErrorResponseBodyIsUsedAsAuthenticationFailureMessage() throws IOException {
        final byte[] errorBody = "{\"error\":\"forbidden\"}".getBytes(StandardCharsets.UTF_8);
        final HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/auth", exchange -> {
            exchange.sendResponseHeaders(403, errorBody.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorBody);
            }
        });
        server.start();
        final String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/auth";
        try {
            final HttpUserAuthenticator auth = new HttpUserAuthenticator(url);
            auth.authenticate("some socket id");
            fail("Expected AuthenticationFailureException");
        } catch (final AuthenticationFailureException e) {
            assertEquals("Server returned HTTP response code: 403 for URL: " + url + ": {\"error\":\"forbidden\"}", e.getMessage());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testEmptyErrorResponseKeepsStatusAndUrlInAuthenticationFailureMessage() throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/auth", exchange -> {
            exchange.sendResponseHeaders(403, -1);
            exchange.close();
        });
        server.start();
        final String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/auth";
        try {
            final HttpUserAuthenticator auth = new HttpUserAuthenticator(url);
            auth.authenticate("some socket id");
            fail("Expected AuthenticationFailureException");
        } catch (final AuthenticationFailureException e) {
            assertEquals("Server returned HTTP response code: 403 for URL: " + url, e.getMessage());
        } finally {
            server.stop(0);
        }
    }
}
