package com.pusher.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Configuration for a {@link com.pusher.client.Pusher} instance.
 */
public class PusherOptions {

    public static final String LIB_VERSION = readVersionFromProperties();

    private static final String URI_SUFFIX = "?client=java-client&protocol=5&version=" + LIB_VERSION;
    private static final String WS_SCHEME = "ws";
    private static final String WSS_SCHEME = "wss";

    private static final int WS_PORT = 80;
    private static final int WSS_PORT = 443;
    private static final String PUSHER_DOMAIN = "pusher.com";

    private static final long DEFAULT_ACTIVITY_TIMEOUT = 120000;
    private static final long DEFAULT_PONG_TIMEOUT = 30000;

    // Note that the primary cluster lives on a different domain
    // (others are subdomains of pusher.com). This is not an oversight.
    // Legacy reasons.
    private String host = "ws.pusherapp.com";
    private int wsPort = WS_PORT;
    private int wssPort = WSS_PORT;
    private boolean encrypted = true;
    private long activityTimeout = DEFAULT_ACTIVITY_TIMEOUT;
    private long pongTimeout = DEFAULT_PONG_TIMEOUT;
    private Authorizer authorizer;

    /**
     * Gets whether an encrypted (SSL) connection should be used when connecting to Pusher.
     * @return true if an encrypted connection should be used; otherwise false.
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Sets an encrypted (SSL) connection should be used when connecting to Pusher.
     * @param encrypted
     * @return this, for chaining
     */
    public PusherOptions setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    /**
     * Gets the authorizer to be used when authenticating private and presence channels.
     * @return the authorizer
     */
    public Authorizer getAuthorizer() {
        return authorizer;
    }

    /**
     * Sets the authorizer to be used when authenticating private and presence channels.
     * @param authorizer The authorizer to be used.
     * @return this, for chaining
     */
    public PusherOptions setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
        return this;
    }

    /**
     * The host to which connections will be made.
     *
     * Note that if you wish to connect to a standard Pusher cluster, the convenience
     * method setCluster will set the host and ports correctly from a single argument.
     *
     * @param hostname
     * @return this, for chaining
     */
    public PusherOptions setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * The port to which unencrypted connections will be made.
     *
     * Note that if you wish to connect to a standard Pusher cluster, the convenience
     * method setCluster will set the host and ports correctly from a single argument.
     *
     * @param non-SSL port number
     * @return this, for chaining
     */
    public PusherOptions setWsPort(int wsPort) {
        this.wsPort = wsPort;
        return this;
    }

    /**
     * The port to which encrypted connections will be made.
     *
     * Note that if you wish to connect to a standard Pusher cluster, the convenience
     * method setCluster will set the host and ports correctly from a single argument.
     *
     * @param SSL port number
     * @return this, for chaining
     */
    public PusherOptions setWssPort(int wssPort) {
        this.wssPort = wssPort;
        return this;
    }

    public PusherOptions setCluster(String cluster) {
        this.host = "ws-" + cluster + "." + PUSHER_DOMAIN;
        this.wsPort = WS_PORT;
        this.wssPort = WSS_PORT;
        return this;
    }

    /**
     * The number of milliseconds of inactivity at which a "ping" will be triggered
     * to check the connection.
     *
     * The default value is 120,000 (2 minutes). On some connections, where
     * intermediate hops between the application and Pusher are aggressively
     * culling connections they consider to be idle, a lower value may help
     * preserve the connection.
     *
     * @param activityTimeout time to consider connection idle, in milliseconds
     * @return this, for chaining
     */
    public PusherOptions setActivityTimeout(long activityTimeout) {
        if (activityTimeout < 1000) {
            throw new IllegalArgumentException("Activity timeout must be at least 1,000ms (and is recommended to be much higher)");
        }

        this.activityTimeout = activityTimeout;
        return this;
    }

    public long getActivityTimeout() {
        return activityTimeout;
    }

    /**
     * The number of milliseconds after a "ping" is sent that the client will
     * wait to receive a "pong" response from the server before considering the
     * connection broken and triggering a transition to the disconnected state.
     *
     * The default value is 30,000.
     *
     * @param pongTimeout time to wait for pong response, in milliseconds
     * @return this, for chaining
     */
    public PusherOptions setPongTimeout(long pongTimeout) {
        if (pongTimeout < 1000) {
            throw new IllegalArgumentException("Pong timeout must be at least 1,000ms (and is recommended to be much higher)");
        }

        this.pongTimeout = pongTimeout;
        return this;
    }

    public long getPongTimeout() {
        return pongTimeout;
    }

    /**
     * Construct the URL for the WebSocket connection based on the options
     * previous set on this object and the provided API key
     * @param apiKey
     * @return the WebSocket URL
     */
    public String buildUrl(String apiKey) {
        return String.format("%s://%s:%s/app/%s%s",
                encrypted ? WSS_SCHEME : WS_SCHEME,
                host,
                encrypted ? wssPort : wsPort,
                apiKey,
                URI_SUFFIX);
    }


    private static String readVersionFromProperties() {
        InputStream inStream = null;
        try {
            Properties p = new Properties();
            inStream = PusherOptions.class.getResourceAsStream("/pusher.properties");
            p.load(inStream);
            String version = (String) p.get("version");
            if (version != null && version.length() > 0) {
                return version;
            }
        }
        catch (Exception e) {
            // Fall back to fixed value
        }
        finally {
            try {
                if (inStream != null) inStream.close();
            }
            catch (IOException e) {
                // Ignore problem closing stream
            }
        }
        return "0.0.0";
    }
}
