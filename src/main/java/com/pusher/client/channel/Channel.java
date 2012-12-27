package com.pusher.client.channel;

public interface Channel {

    String getName();
    
    void bind(String eventName, ChannelEventListener listener);
}