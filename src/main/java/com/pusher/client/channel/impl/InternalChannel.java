package com.pusher.client.channel.impl;

import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PusherEvent;

public interface InternalChannel extends Channel, Comparable<InternalChannel> {

    String toSubscribeMessage(String authResponse);

    String toUnsubscribeMessage();

    PusherEvent prepareEvent(String event, String message);

    void onMessage(String event, String message);

    void updateState(ChannelState state);

    ChannelState getState();

    void setEventListener(ChannelEventListener listener);

    ChannelEventListener getEventListener();
}
