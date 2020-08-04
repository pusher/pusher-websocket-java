package com.pusher.client.channel.impl;

import java.util.Map;

public class AuthResponseData {
    private Long sendMillis = null;
    private Long receivedMilis = null;
    private Map<String, PusherAuthResponse> response = null;
    private Boolean hasToken = null;
    private String channels = null;


    public AuthResponseData(Long send, Long received, Map<String, PusherAuthResponse> data, Boolean hasToken, String channels) {
        this.sendMillis = send;
        this.receivedMilis = received;
        this.response = data;
        this.hasToken = hasToken;
        this.channels = channels;
    }

    private Long getRemaining() {
        if (sendMillis == null) return null;
        if (receivedMilis == null) return null;
        if ((receivedMilis - sendMillis) <=0) return 0L;
        return receivedMilis - sendMillis;
    }

    public Map<String, PusherAuthResponse> getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "AuthResponseData{" +
                "sendMillis=" + sendMillis +
                ", receivedMilis=" + receivedMilis +
                ", cost=" + getRemaining() +
                ", response='" + response + '\'' +
                ", hasToken=" + hasToken +
                ", channels='" + channels + '\'' +
                '}';
    }
}
