package com.pusher.client.channel;

import java.util.Map;

public class PusherEvent {
    private Map<String, Object> eventData;

    /**
     * PusherEvent constructor that takes a hashmap.
     * @param eventData hashmap(string, string)
     */
    public PusherEvent(Map<String, Object> eventData) {
        this.eventData = eventData;
    }
    /**
     * getProperty returns the value associated with the key, or null.
     * @param key - the key you wish to get
     * @return value (string) - the value as a string.
     */
    public Object getProperty(String key) {
        return eventData.get(key);
    }


    /**
     * returns the userId associated with this event.
     * @return the userID string: https://pusher.com/docs/channels/using_channels/events#user-id-in-client-events,
     * or null if the customer if the event is not a client event.
     */
    public Object getUserId() {
        return eventData.get("user_id");
    }
    public Object getChannelName() { return eventData.get("channel"); }
    public Object getEventName() { return eventData.get("event"); }
    public Object getData() { return eventData.get("data"); }


    public String toString() {
        String returnString = "";
        for (String key: eventData.keySet()){
            Object value = eventData.get(key);
            returnString = returnString + key + ": " + value.toString() + ", ";
        }
        return returnString;
    }
}
