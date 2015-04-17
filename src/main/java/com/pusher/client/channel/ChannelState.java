package com.pusher.client.channel;

/**
 * Used to identify the state of the channel e.g. subscribed or unsubscribed.
 */
public enum ChannelState {
    INITIAL, SUBSCRIBE_SENT, SUBSCRIBED, UNSUBSCRIBED, FAILED
}
