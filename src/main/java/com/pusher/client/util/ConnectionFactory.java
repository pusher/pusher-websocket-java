package com.pusher.client.util;

/**
 * Abstract factory to be used for
 * building HttpAuthorizer connections
 */
public abstract class ConnectionFactory {
    private String channelName;
    private String socketId;

    public ConnectionFactory() {
    }

    public abstract String getBody();

    public abstract String getCharset();

    public abstract String getContentType();

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }
}
