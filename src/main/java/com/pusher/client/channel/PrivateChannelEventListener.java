package com.pusher.client.channel;

public interface PrivateChannelEventListener extends ChannelEventListener {

    void onAuthenticationFailure(String message, Exception e);
}