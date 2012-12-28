package com.pusher.client.channel;

public interface ChannelEventListener {

    void onEvent(String channelName, String eventName, String data);
    
    void onSubscriptionSucceeded(Channel channel);
}