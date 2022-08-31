package com.pusher.client.channel.impl.message;

import java.util.HashMap;
import java.util.Map;

public class UnsubscribeMessage {

    private final String event = "pusher:unsubscribe";
    private final Map<String, String> data = new HashMap<>();

    public UnsubscribeMessage(String channelName) {
        data.put("channel", channelName);
    }
}
