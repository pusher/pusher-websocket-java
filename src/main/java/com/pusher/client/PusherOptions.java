package com.pusher.client;


/**
 * Configuration for a {@link com.pusher.client.Pusher} instance.
 */
public class PusherOptions {

    // The version is populated from the pom.xml when running the application as a
    // built library. However when running
    // the source locally this will return null, so a default version of 0.0.0
    // will be used instead.
    private static final String APP_VERSION =
            PusherOptions.class.getPackage().getImplementationVersion() != null ?
                    PusherOptions.class.getPackage().getImplementationVersion() : "0.0.0";

    private static final String URI_SUFFIX = "?client=java-client&protocol=5&version=" + APP_VERSION;
    private static final String WS_SCHEME = "ws";
    private static final String WSS_SCHEME = "wss";

    private static final int WS_PORT = 80;
    private static final int WSS_PORT = 443;
    private static final String PUSHER_DOMAIN = "pusher.com";

    // Note that the primary cluster lives on a different domain
    // (others are subdomains of pusher.com). This is not an oversight.
    // Legacy reasons.
    private String host = "ws.pusherapp.com";
    private int wsPort = WS_PORT;
    private int wssPort = WSS_PORT;
    private boolean encrypted = true;
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
}
