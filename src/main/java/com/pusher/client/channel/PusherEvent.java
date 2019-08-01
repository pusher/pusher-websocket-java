package com.pusher.client.channel;

import java.util.Map;

public class PusherEvent {
    private Map<String, String> eventData;

    /**
     * PusherEvent constructor that takes a hashmap.
     * @param eventData hashmap(string, string)
     */
    public PusherEvent(Map<String, String> eventData) {
        this.eventData = eventData;
    }
    /**
     * getProperty returns the value associated with the key, or null.
     * @param key - the key you wish to get
     * @return value (string) - the value as a string.
     */
    public String getProperty(String key) {
        return eventData.get(key);
    }


    /**
     * returns the userId associated with this event.
     * @return the userID string: https://pusher.com/docs/channels/using_channels/events#user-id-in-client-events,
     * or null if the customer if the event is not a client event.
     */
    public String getUserId() {
        return eventData.get("user_id");
    }
    public String getChannelName() { return eventData.get("channel"); }
    public String getEventName() { return eventData.get("event"); }
    public String getData() { return eventData.get("data"); }


    public String toString() {
        String returnString = "";
        for (String key: eventData.keySet()){
            String value = eventData.get(key);
            returnString = returnString + key + ": " + value.toString() + ", ";
        }
        return returnString;
    }
}
