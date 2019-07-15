package com.pusher.client.channel;

import java.util.Map;

/**
 * EventMetadata stores metadata additional data concerning an event. This data
 * is not essential to using Channels, but may be useful for your service.
 */
public class EventMetadata {
    private Map<String, String> metadata;

    /**
     * EventMetadata constructor that takes a hashmap.
     * @param metadata hashmap(string, string)
     */
    public EventMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    /**
     * getMetadataAttr returns the value associated with the key, or null.
     * @param key - the key you wish to get
     * @return value (string) - the value as a string.
     */
    public String getMetadataAttr(String key) {
        return metadata.get(key);
    }


    /**
     * returns the userId associated with this events metadata.
     * This is a helper method intended to help with discovery as to which
     * attributes are available in the metadata object.
     * @return the userID string, or null.
     */
    public String getUserId() {
        return metadata.get("user_id");
    }

    public String toString() {
        String returnString = "";
        if(metadata!=null) {
            for (String key: metadata.keySet()){
                String value = metadata.get(key);
                returnString = returnString + key + ": " + value + ", ";
            }
        }
        return returnString;
    }
}