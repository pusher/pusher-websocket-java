package com.pusher.client;

/**
 * Options to be used with a {@link com.pusher.client.Pusher} instance.
 */
public class PusherOptions {

    // The version is populated from the pom.xml when running the application as a
    // built library. However when running
    // the source locally this will return null, so a default version of 0.0.0
    // will be used instead.
    private static final String APP_VERSION = (PusherOptions.class
            .getPackage().getImplementationVersion() != null) ? PusherOptions.class
            .getPackage().getImplementationVersion() : "0.0.0";
    private static final String WS_SCHEME = "ws";
    private static final String WSS_SCHEME = "wss";
    private static final String HOST = "ws.pusherapp.com";
    private static final int WS_PORT = 80;
    private static final int WSS_PORT = 443;
    private static final String URI_SUFFIX = "?client=java-client&protocol=5&version=" + APP_VERSION;

    private boolean encrypted = true;
    private Authorizer authorizer;

    private String host;
    private Integer wsPort;
    private Integer wssPort;

    /**
     * Gets whether an encrypted (SSL) connection should be used when connecting to Pusher.
     *
     * @return true if an encrypted connection should be used; otherwise false.
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Sets an encrypted (SSL) connection should be used when connecting to Pusher.
     *
     * @param encrypted
     * @return this, for chaining
     */
    public PusherOptions setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    /**
     * Gets the authorizer to be used when authenticating private and presence channels.
     *
     * @return the authorizer
     */
    public Authorizer getAuthorizer() {
        return authorizer;
    }

    /**
     * Sets the authorizer to be used when authenticating private and presence channels.
     *
     * @param authorizer The authorizer to be used.
     * @return this, for chaining
     */
    public PusherOptions setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
        return this;
    }

    /**
     * Gets the host to be used when generating the url for the connection.
     *
     * @return the default host if there is no one configured or returns the configured host
     *
     */
    public String getHost() {
        if(host == null) {
            return HOST;
        }
        return host;
    }

    /**
     * Sets a new host configuration.
     *
     * @param host
     * @return this, for chaining
     */
    public PusherOptions setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Gets the ws port to be used when generating the url for the connection.
     *
     * @return the default ws port if there is no one configured or returns the configured ws port.
     */
    public Integer getWsPort() {
        if(wsPort == null) {
            return WS_PORT;
        }
        return wsPort;
    }

    /**
     * Sets a new ws port configuration.
     *
     * @param wsPort
     * @return this, for chaining
     */
    public PusherOptions setWsPort(Integer wsPort) {
        this.wsPort = wsPort;
        return this;
    }

    /**
     * Gets the wss port to be used when generating the url for the connection.
     *
     * @return the default wss port if there is no one configured or returns the configured wss port.
     */
    public Integer getWssPort() {
        if(wssPort == null) {
            return WSS_PORT;
        }
        return wssPort;
    }

    /**
     * Sets a new wss port configuration.
     *
     * @param wssPort
     * @return this, for chaining
     */
    public PusherOptions setWssPort(Integer wssPort) {
        this.wssPort = wssPort;
        return this;
    }

    /**
     * Generate the complete URL for a given token to be used when connecting.
     *
     * @param apiKey
     * @return the complete url
     */
    public String generateURLFor(String apiKey) {
        return String.format("%s://%s:%s/app/%s%s", (isEncrypted() ? WSS_SCHEME : WS_SCHEME), getHost(),
                (isEncrypted() ? getWssPort() : getWsPort()), apiKey, URI_SUFFIX);
    }

}
