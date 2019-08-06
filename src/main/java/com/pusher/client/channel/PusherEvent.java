package com.pusher.client.channel;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

public class PusherEvent {
    private JsonObject eventData;

    /**
     * PusherEvent constructor that takes a JsonObject.
     * @param eventData JsonObject;
     */
    public PusherEvent(JsonObject eventData) {
        this.eventData = eventData;
    }
    /**
     * Note: the API of this method is not stable we recommend using the specialized getters.
     * getProperty returns the value associated with the key, or null.
     * @param key - the key you wish to get
     * @return value (JsonElement) - the value as an JsonElement.
     */
    public JsonElement getProperty(String key) {
        return eventData.get(key);
    }


    /**
     * returns the userId associated with this event.
     * @return the userID string: https://pusher.com/docs/channels/using_channels/events#user-id-in-client-events,
     * or null if the customer if the event is not a client event.
     */
    public String getUserId() { return eventData.get("user_id").getAsString(); }
    public String getChannelName() { return eventData.get("channel").getAsString(); }
    public String getEventName() { return eventData.get("event").getAsString(); }
    public String getData() { return eventData.get("data").getAsString(); }

    public String toString() {
        return eventData.toString();
    }
}
