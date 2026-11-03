package com.pusher.client.connection.impl;

import com.pusher.client.channel.PusherEvent;

/**
 * Receives events from the connection.
 * <p>
 * Used instead of java.util.function.Consumer, which isn't available before Android 7 (API 24).
 */
public interface PusherEventHandler {
    void handle(PusherEvent event);
}
