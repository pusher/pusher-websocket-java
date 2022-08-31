package com.pusher.client.util;

import com.pusher.client.channel.PusherEvent;

public interface PusherEventHandler {
    void handleEvent(PusherEvent event);
}
