package com.pusher.client.channel;

import java.util.Map;

/**
 * PusherEvent stores all data concerning an event. This data
 * includes things that are not essential to using Channels,
 * but may be useful for your service.
 */
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
     * This is a helper method intended to help with discovery as to which
     * attributes are available in the eventData object.
     * @return the userID string, or null.
     */
    public Object getUserId() {
        return eventData.get("user_id");
    }

    public Object getChannel() { return eventData.get("channel"); }
    public Object getEventName() { return eventData.get("event"); }
    public Object getData() { return eventData.get("data"); }


    public String toString() {
        String returnString = "";
        if(eventData !=null) {
            for (String key: eventData.keySet()){
                Object value = eventData.get(key);
                returnString = returnString + key + ": " + value.toString() + ", ";
            }
        }
        return returnString;
    }
}
