package com.pusher.client.channel;

import java.util.Map;

public class PusherEvent {
    private Map<String, Object> eventData;

    public PusherEvent(Map<String, Object> eventData) {
        this.eventData = eventData;
    }

    /**
     * getProperty returns the value associated with the key, or null.
     * It is recommended that you use the specialized getters in this class instead.
     *
     * @param key The key you wish to get.
     * @return
     *      The object can be casted as follows:
     *      - JSON strings - java.lang.String
     *      - JSON number - java.lang.Double
     *      - JSON boolean - java.lang.Boolean
     *      - JSON array - java.util.ArrayList
     *      - JSON object - java.util.Map
     *      - JSON null - null
     */
    public Object getProperty(String key) {
        return eventData.get(key);
    }

    /**
     * Returns the userId associated with this event.
     *
     * @return
     *      The userID string: https://pusher.com/docs/channels/using_channels/events#user-id-in-client-events,
     *      or null if the customer if the event is not a client event.
     */
    public String getUserId() { return (String)eventData.get("user_id"); }
    public String getChannelName() { return (String)eventData.get("channel"); }
    public String getEventName() { return (String)eventData.get("event"); }
    public String getData() { return (String)eventData.get("data"); }

    public String toString() {
        return eventData.toString();
    }
}
