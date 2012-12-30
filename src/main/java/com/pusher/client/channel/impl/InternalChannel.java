package com.pusher.client.channel.impl;

import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelState;

public interface InternalChannel extends Channel {
    
    String toSubscribeMessage();

    String toUnsubscribeMessage();

    void onMessage(String event, String message);
    
    void updateState(ChannelState subscribeSent);
}