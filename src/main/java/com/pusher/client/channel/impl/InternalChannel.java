package com.pusher.client.channel.impl;

import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelState;

public interface InternalChannel extends Channel, Comparable<InternalChannel> {
    
    String toSubscribeMessage(String... extraArguments);

    String toUnsubscribeMessage();

    void onMessage(String event, String message);
    
    void updateState(ChannelState state);
}