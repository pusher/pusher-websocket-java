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
     * @return value (Object) - the value as a string.
     */
    public Object getProperty(String key) {
        return eventData.get(key);
    }


    /**
     * returns the userId associated with this event.
     * @return the userID string: https://pusher.com/docs/channels/using_channels/events#user-id-in-client-events,
     * or null if the customer if the event is not a client event.
     */
    public String getUserId() {return eventData.get("user_id").toString(); }
    public String getChannelName() { return eventData.get("channel").toString(); }
    public String getEventName() { return eventData.get("event").toString(); }
    public String getData() { return eventData.get("data").toString(); }


    public String toString() {
        String returnString = "";
        for (String key: eventData.keySet()){
            String value = eventData.get(key).toString();
            returnString = returnString + key + ": " + value.toString() + ", ";
        }
        return returnString;
    }
}
