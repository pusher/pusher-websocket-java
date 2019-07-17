package com.pusher.client.channel;

import java.util.Map;

/**
 * PusherEvent stores all data concerning an event. This data
 * includes things that are not essential to using Channels,
 * but may be useful for your service.
 */
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
     * This is a helper method intended to help with discovery as to which
     * attributes are available in the eventData object.
     * @return the userID string, or null.
     */
    public String getUserId() {
        return eventData.get("user_id");
    }

    public String getChannel() { return eventData.get("channel"); }
    public String getEventName() { return eventData.get("event"); }
    public String getData() { return eventData.get("data"); }


    public String toString() {
        String returnString = "";
        if(eventData !=null) {
            for (String key: eventData.keySet()){
                String value = eventData.get(key);
                returnString = returnString + key + ": " + value + ", ";
            }
        }
        return returnString;
    }
}
