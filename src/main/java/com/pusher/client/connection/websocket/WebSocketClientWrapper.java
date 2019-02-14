package com.pusher.client.connection.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;

import com.pusher.java_websocket.client.WebSocketClient;
import com.pusher.java_websocket.handshake.ServerHandshake;

/**
 * A thin wrapper around the WebSocketClient class from the Java-WebSocket
 * library. The purpose of this class is to enable the WebSocketConnection class
 * to be unit tested by swapping out an instance of this wrapper for a mock
 * version.
 */
public class WebSocketClientWrapper extends WebSocketClient {

    private static final String WSS_SCHEME = "wss";
    private WebSocketListener webSocketListener;

    public WebSocketClientWrapper(final URI uri, final Proxy proxy, final WebSocketListener webSocketListener) throws SSLException {
        super(uri);

        if (uri.getScheme().equals(WSS_SCHEME)) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null); // will use java's default
                                                   // key and trust store which
                                                   // is sufficient unless you
                                                   // deal with self-signed
                                                   // certificates

                final SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory)
                                                                               // SSLSocketFactory.getDefault();
                Socket socket;
                if(proxy != Proxy.NO_PROXY){

                    InetSocketAddress socketAddress = (InetSocketAddress) proxy.address();

                    Socket tunnel = new Socket(socketAddress.getHostName(), socketAddress.getPort());
                    doTunnelHandshake(tunnel, uri.getHost(), uri.getPort(), proxy.address());

                    socket = factory.createSocket(tunnel, uri.getHost(), uri.getPort(), true);

                }
                else{
                    socket = factory.createSocket();
                }


                setSocket(socket);
            }
            catch (final IOException e) {
                throw new SSLException(e);
            }
            catch (final NoSuchAlgorithmException e) {
                throw new SSLException(e);
            }
            catch (final KeyManagementException e) {
                throw new SSLException(e);
            }
        }
        this.webSocketListener = webSocketListener;
        setProxy(proxy);
    }

    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        if (webSocketListener != null) {
            webSocketListener.onOpen(handshakedata);
        }
    }

    @Override
    public void onMessage(final String message) {
        if (webSocketListener != null) {
            webSocketListener.onMessage(message);
        }
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        if (webSocketListener != null) {
            webSocketListener.onClose(code, reason, remote);
        }
    }

    @Override
    public void onError(final Exception ex) {
        if (webSocketListener != null) {
            webSocketListener.onError(ex);
        }
    }

    /**
     * Removes the WebSocketListener so that the underlying WebSocketClient doesn't expose any listener events.
     */
    public void removeWebSocketListener() {
        webSocketListener = null;
    }


    /*
     *
     * Copyright (c) 1994, 2004, Oracle and/or its affiliates. All rights reserved.
     *
     * Redistribution and use in source and binary forms, with or
     * without modification, are permitted provided that the following
     * conditions are met:
     *
     * -Redistribution of source code must retain the above copyright
     * notice, this list of conditions and the following disclaimer.
     *
     * Redistribution in binary form must reproduce the above copyright
     * notice, this list of conditions and the following disclaimer in
     * the documentation and/or other materials provided with the
     * distribution.
     *
     * Neither the name of Oracle nor the names of
     * contributors may be used to endorse or promote products derived
     * from this software without specific prior written permission.
     *
     * This software is provided "AS IS," without a warranty of any
     * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
     * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
     * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
     * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
     * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
     * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
     * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
     * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
     * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
     * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
     * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
     *
     * You acknowledge that this software is not designed, licensed or
     * intended for use in the design, construction, operation or
     * maintenance of any nuclear facility.
     */

    private void doTunnelHandshake(Socket tunnel, String host, int port, SocketAddress tunnelAddress) throws IOException {
        OutputStream out = tunnel.getOutputStream();
        String msg = "CONNECT " + host + ":" + port + " HTTP/1.0\n"
                + "User-Agent: "
                + sun.net.www.protocol.http.HttpURLConnection.userAgent
                + "\r\n\r\n";
        byte[] b;
        try {
            /*
             * We really do want ASCII7 -- the http protocol doesn't change
             * with locale.
             */
            b = msg.getBytes("ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            /*
             * If ASCII7 isn't there, something serious is wrong, but
             * Paranoia Is Good (tm)
             */
            b = msg.getBytes();
        }
        out.write(b);
        out.flush();

        /*
         * We need to store the reply so we can create a detailed
         * error message to the user.
         */
        byte[]           reply = new byte[200];
        int             replyLen = 0;
        int             newlinesSeen = 0;
        boolean         headerDone = false;     /* Done on first newline */

        InputStream     in = tunnel.getInputStream();
        boolean         error = false;

        while (newlinesSeen < 2) {
            int i = in.read();
            if (i < 0) {
                throw new IOException("Unexpected EOF from proxy");
            }
            if (i == '\n') {
                headerDone = true;
                ++newlinesSeen;
            } else if (i != '\r') {
                newlinesSeen = 0;
                if (!headerDone && replyLen < reply.length) {
                    reply[replyLen++] = (byte) i;
                }
            }
        }

        /*
         * Converting the byte array to a string is slightly wasteful
         * in the case where the connection was successful, but it's
         * insignificant compared to the network overhead.
         */
        String replyStr;
        try {
            replyStr = new String(reply, 0, replyLen, "ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            replyStr = new String(reply, 0, replyLen);
        }

        /* We asked for HTTP/1.0, so we should get that back */
        if (!replyStr.startsWith("HTTP/1.0 200")) {
            throw new IOException("Unable to tunnel through "
                    + tunnelAddress
                    + ".  Proxy returns \"" + replyStr + "\"");
        }

        /* tunneling Handshake was successful! */
    }
}
