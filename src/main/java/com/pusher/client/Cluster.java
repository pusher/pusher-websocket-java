package com.pusher.client;

/**
 * Host and port information for WebSocket end-points.
 *
 * General purpose Pusher clusters provided as constants, if
 * you need to connect to a custom end-point, e.g. a test server,
 * just construct your own and set it on your PusherOptions.
 */
public class Cluster {

    public static final Cluster US_EAST = new Cluster("ws.pusherapp.com", 80, 443);
    public static final Cluster EU = new Cluster("ws-eu.pusherapp.com", 80, 443);

    private final String host;
    private final int wsPort;
    private final int wssPort;

    public Cluster(final String host, final int wsPort, final int wssPort) {
        this.host = host;
        this.wsPort = wsPort;
        this.wssPort = wssPort;
    }

    public String getHost() {
        return host;
    }

    public int getWsPort() {
        return wsPort;
    }

    public int getWssPort() {
        return wssPort;
    }
}
