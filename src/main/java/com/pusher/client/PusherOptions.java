package com.pusher.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.Properties;

/**
 * Configuration for a {@link com.pusher.client.Pusher} instance.
 */
public class PusherOptions {

    private static final String SRC_LIB_DEV_VERSION = "@version@";
    private static final String LIB_DEV_VERSION = "0.0.0-dev";
    public static final String LIB_VERSION = readVersionFromProperties();

    private static final String URI_SUFFIX = "?client=java-client&protocol=5&version=" + LIB_VERSION;
    private static final String WS_SCHEME = "ws";
    private static final String WSS_SCHEME = "wss";

    private static final int WS_PORT = 80;
    private static final int WSS_PORT = 443;
    private static final String PUSHER_DOMAIN = "pusher.com";

    private static final long DEFAULT_ACTIVITY_TIMEOUT = 120000;
    private static final long DEFAULT_PONG_TIMEOUT = 30000;

    private static final int MAX_RECONNECTION_ATTEMPTS = 6; //Taken from the Swift lib
    private static final int MAX_RECONNECT_GAP_IN_SECONDS = 30;

    // Note that the primary cluster lives on a different domain
    // (others are subdomains of pusher.com). This is not an oversight.
    // Legacy reasons.
    private String host = "ws.pusherapp.com";
    private int wsPort = WS_PORT;
    private int wssPort = WSS_PORT;
    private boolean useTLS = true;
    private long activityTimeout = DEFAULT_ACTIVITY_TIMEOUT;
    private long pongTimeout = DEFAULT_PONG_TIMEOUT;
    private UserAuthenticator userAuthenticator;
    private ChannelAuthorizer channelAuthorizer;
    private Authorizer authorizer;
    private Proxy proxy = Proxy.NO_PROXY;
    private int maxReconnectionAttempts = MAX_RECONNECTION_ATTEMPTS;
    private int maxReconnectGapInSeconds = MAX_RECONNECT_GAP_IN_SECONDS;

    /**
     * @deprecated
     * Please use isUseTLS
     */
    @Deprecated
    public boolean isEncrypted() {
        return useTLS;
    }

    /**
     * @deprecated
     * Please use setUseTLS
     */
    @Deprecated
    public PusherOptions setEncrypted(final boolean encrypted) {
        this.useTLS = encrypted;
        return this;
    }

    /**
     *
     * @return whether the connection to Pusher should use TLS
     */
    public boolean isUseTLS() {
        return useTLS;
    }

    /**
     * Sets whether the connection to Pusher should be use TLS.
     * @param useTLS whether the connection should use TLS, by default this is true
     * @return this, for chaining
     */
    public PusherOptions setUseTLS(final boolean useTLS) {
        this.useTLS = useTLS;
        return this;
    }

    /**
     * Gets the user authenticator to be used when signing in.
     *
     * @return the user authenticator
     */
    public UserAuthenticator getUserAuthenticator() {
        return userAuthenticator;
    }

    /**
     * Sets the user authenticator to be used when signing in.
     *
     * @param userAuthenticator
     *            The user authenticator to be used.
     * @return this, for chaining
     */
    public PusherOptions setUserAuthenticator(final UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
        return this;
    }

    /**
     * Gets the channel authorizer to be used when authorizing private and presence
     * channels.
     *
     * @return the channel authorizer
     */
    public ChannelAuthorizer getChannelAuthorizer() {
        return channelAuthorizer;
    }

    /**
     * Sets the channel authorizer to be used when authorizing private and presence
     * channels.
     *
     * @param channelAuthorizer
     *            The channel authorizer to be used.
     * @return this, for chaining
     */
    public PusherOptions setChannelAuthorizer(final ChannelAuthorizer channelAuthorizer) {
        this.channelAuthorizer = channelAuthorizer;
        return this;
    }

    /**
     * @deprecated
     * Please use getChannelauthorizer
     */
    @Deprecated
    public Authorizer getAuthorizer() {
        return authorizer;
    }

    /**
     * @deprecated
     * Please use setChannelauthorizer
     */
    @Deprecated
    public PusherOptions setAuthorizer(final Authorizer authorizer) {
        this.authorizer = authorizer;
        return setChannelAuthorizer(authorizer);
    }

    /**
     * The host to which connections will be made.
     *
     * Note that if you wish to connect to a standard Pusher cluster, the
     * convenience method setCluster will set the host and ports correctly from
     * a single argument.
     *
     * @param host The host
     * @return this, for chaining
     */
    public PusherOptions setHost(final String host) {
        this.host = host;
        return this;
    }

    /**
     * The port to which non TLS connections will be made.
     *
     * Note that if you wish to connect to a standard Pusher cluster, the
     * convenience method setCluster will set the host and ports correctly from
     * a single argument.
     *
     * @param wsPort port number
     * @return this, for chaining
     */
    public PusherOptions setWsPort(final int wsPort) {
        this.wsPort = wsPort;
        return this;
    }

    /**
     * The port to which encrypted connections will be made.
     *
     * Note that if you wish to connect to a standard Pusher cluster, the
     * convenience method setCluster will set the host and ports correctly from
     * a single argument.
     *
     * @param wssPort port number
     * @return this, for chaining
     */
    public PusherOptions setWssPort(final int wssPort) {
        this.wssPort = wssPort;
        return this;
    }

    public PusherOptions setCluster(final String cluster) {
        host = "ws-" + cluster + "." + PUSHER_DOMAIN;
        wsPort = WS_PORT;
        wssPort = WSS_PORT;
        return this;
    }

    /**
     * The number of milliseconds of inactivity at which a "ping" will be
     * triggered to check the connection.
     *
     * The default value is 120,000 (2 minutes). On some connections, where
     * intermediate hops between the application and Pusher are aggressively
     * culling connections they consider to be idle, a lower value may help
     * preserve the connection.
     *
     * @param activityTimeout
     *            time to consider connection idle, in milliseconds
     * @return this, for chaining
     */
    public PusherOptions setActivityTimeout(final long activityTimeout) {
        if (activityTimeout < 1000) {
            throw new IllegalArgumentException(
                    "Activity timeout must be at least 1,000ms (and is recommended to be much higher)");
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
     * @param pongTimeout
     *            time to wait for pong response, in milliseconds
     * @return this, for chaining
     */
    public PusherOptions setPongTimeout(final long pongTimeout) {
        if (pongTimeout < 1000) {
            throw new IllegalArgumentException(
                    "Pong timeout must be at least 1,000ms (and is recommended to be much higher)");
        }

        this.pongTimeout = pongTimeout;
        return this;
    }

    /**
     * Number of reconnect attempts when websocket connection failed
     * @param maxReconnectionAttempts
     *                 number of max reconnection attempts, default = {@link #MAX_RECONNECTION_ATTEMPTS} 6
     * @return this, for chaining
     */
    public PusherOptions setMaxReconnectionAttempts(int maxReconnectionAttempts) {
        this.maxReconnectionAttempts = maxReconnectionAttempts;
        return this;
    }

    /**
     * The delay in two reconnection extends exponentially (1, 2, 4, .. seconds) This property sets the maximum in between two
     * reconnection attempts.
     * @param maxReconnectGapInSeconds
     *                 time in seconds of the maximum gab between two reconnection attempts, default = {@link #MAX_RECONNECT_GAP_IN_SECONDS} 30s
     * @return this, for chaining
     */
    public PusherOptions setMaxReconnectGapInSeconds(int maxReconnectGapInSeconds) {
        this.maxReconnectGapInSeconds = maxReconnectGapInSeconds;
        return this;
    }

    public long getPongTimeout() {
        return pongTimeout;
    }

    /**
     * Construct the URL for the WebSocket connection based on the options
     * previous set on this object and the provided API key
     *
     * @param apiKey The API key
     * @return the WebSocket URL
     */
    public String buildUrl(final String apiKey) {
        return String.format("%s://%s:%s/app/%s%s", useTLS ? WSS_SCHEME : WS_SCHEME, host, useTLS ? wssPort
                : wsPort, apiKey, URI_SUFFIX);
    }

    /**
     *
     * The default value is Proxy.NO_PROXY.
     *
     * @param proxy
     *            Specify a proxy, e.g. <code>options.setProxy( new Proxy( Proxy.Type.HTTP, new InetSocketAddress( "proxyaddress", 80 ) ) )</code>;
     * @return this, for chaining
     */
    public PusherOptions setProxy(Proxy proxy){
        if (proxy == null) {
          throw new IllegalArgumentException("proxy must not be null (instead use Proxy.NO_PROXY)");
        }
        this.proxy = proxy;
        return this;
    }

    /**
     * @return The proxy to be used when opening a websocket connection to Pusher.
     */
    public Proxy getProxy() {
        return this.proxy;
    }

    /**
     * @return the maximum reconnection attempts
     */
    public int getMaxReconnectionAttempts() {
        return maxReconnectionAttempts;
    }

    /**
     * @return the maximum reconnection gap in seconds
     */
    public int getMaxReconnectGapInSeconds() {
        return maxReconnectGapInSeconds;
    }

    private static String readVersionFromProperties() {
        InputStream inStream = null;
        try {
            final Properties p = new Properties();
            inStream = PusherOptions.class.getResourceAsStream("/pusher.properties");
            p.load(inStream);
            String version = (String)p.get("version");

            // If the properties file contents indicates the version is being run
            // from source then replace with a dev indicator. Otherwise the Pusher
            // Socket API will reject the connection.
            if(version.equals(SRC_LIB_DEV_VERSION)) {
                version = LIB_DEV_VERSION;
            }

            if (version != null && version.length() > 0) {
                return version;
            }
        }
        catch (final Exception e) {
            // Fall back to fixed value
        }
        finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            }
            catch (final IOException e) {
                // Ignore problem closing stream
            }
        }
        return "0.0.0";
    }
}
