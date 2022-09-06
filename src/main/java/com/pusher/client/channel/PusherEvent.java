package com.pusher.client.channel;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class PusherEvent {

    @SerializedName("user_id")
    private final String userId;
    private final String data;
    private final String channel;
    private final String event;

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
        switch(key) {
            case "user_id":
                return getUserId();
            case "channel":
                return getChannelName();
            case "data":
                return getData();
            case "event":
                return getEventName();
            default:
                return null;
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getChannelName() {
        return channel;
    }

    public String getEventName() {
        return event;
    }

    public String getData() {
        return data;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public PusherEvent(String event, String channel, String userId, String data) {
        this.event = event;
        this.channel = channel;
        this.userId = userId;
        this.data = data;
    }

    public PusherEvent(String event, String channel, String userId, Map<String, Object> data) {
        this(event, channel, userId, new Gson().toJson(data));
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static PusherEvent fromJson(String json) {
        Gson gson = new Gson();
        JsonObject o = gson.fromJson(json, JsonObject.class);

        String dataJson = "{}";
        JsonElement data = o.has("data") ? o.get("data") : null;
        if (data != null) {
            // It's possible that the data member was already correct JSON,
            // So the Gson parser made it a JsonObject.
            // We need the data member to be a JSON string.
            if (data.isJsonPrimitive()) {
                dataJson = data.getAsString();
            } else {
                dataJson = gson.toJson(data);
            }
        }

        return new PusherEvent(
                o.has("event") ? o.get("event").getAsString() : "",
                o.has("channel") ? o.get("channel").getAsString() : "",
                o.has("user_id") ? o.get("user_id").getAsString() : "",
                dataJson
        );
    }
}
