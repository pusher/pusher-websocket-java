package com.pusher.client.connection.websocket;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * A thin wrapper around the WebSocketClient class from the Java-WebSocket library. The purpose of this
 * class is to enable the WebSocketConnection class to be unit tested by swapping out an instance of
 * this wrapper for a mock version. 
 */
public class WebSocketClientWrapper extends WebSocketClient {

	private static final String WSS_SCHEME = "wss";
	private final WebSocketListener proxy;
    
	public WebSocketClientWrapper(URI uri, WebSocketListener proxy)
			throws SSLException {
		super(uri);

		if (uri.getScheme().equals( WSS_SCHEME )) {
			try {
				SSLContext sslContext = null;
				sslContext = SSLContext.getInstance( "TLS" );
				sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

				SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();

				this.setSocket( factory.createSocket() );
			}
			catch (IOException e) {
				throw new SSLException(e);
			}
			catch (NoSuchAlgorithmException e) {
				throw new SSLException(e);
			}
			catch (KeyManagementException e) {
				throw new SSLException(e);
			}
		}

		this.proxy = proxy;
	}
    
    @Override
    public void onOpen(ServerHandshake handshakedata) {
	proxy.onOpen(handshakedata);
    }

    @Override
    public void onMessage(String message) {
	proxy.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
	proxy.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
	proxy.onError(ex);
    }    
}