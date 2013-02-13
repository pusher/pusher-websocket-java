package com.pusher.client.channel;

public enum ChannelState {
    INITIAL,
    SUBSCRIBE_SENT,
    SUBSCRIBED,
    UNSUBSCRIBED,
    FAILED
}