package com.pusher.client.channel;

public interface PrivateChannelEventListener extends ChannelEventListener {

    void onAuthenticationFailure();
}