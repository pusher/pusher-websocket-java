package com.pusher.client.channel;

public interface InternalChannel extends Channel {
    
    String toSubscribeMessage();

    String toUnsubscribeMessage();

    void onMessage(String event, String message);
    
    void updateState(ChannelState subscribeSent);
}