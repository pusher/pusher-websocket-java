package com.pusher.client.channel.impl;

import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PusherEvent;

public interface InternalChannel extends Channel, Comparable<InternalChannel> {
    String toSubscribeMessage();

    String toUnsubscribeMessage();

    Integer getCount();

    void handleEvent(PusherEvent event);

    void updateState(ChannelState state);

    void setEventListener(ChannelEventListener listener);

    ChannelEventListener getEventListener();
}
