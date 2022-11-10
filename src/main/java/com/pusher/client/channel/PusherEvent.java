package com.pusher.client.channel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class PusherEvent {

    private JsonObject jsonObject = new JsonObject();

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
                return jsonObject.get("data");
            case "event":
                return getEventName();
            default:
                return null;
        }
    }

    public String getUserId() {
        return jsonObject.has("user_id") ? jsonObject.get("user_id").getAsString() : null;
    }

    public String getChannelName() {
        return jsonObject.has("channel") ? jsonObject.get("channel").getAsString() : null;
    }

    public String getEventName() {
        return jsonObject.has("event") ? jsonObject.get("event").getAsString() : null;
    }

    public String getData() {
        JsonElement data = jsonObject.get("data");
        if (data.isJsonPrimitive()) {
            return data.getAsString();
        }
        final Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
        return gson.toJson(data);
    }

    public String toString() {
        return this.toJson();
    }

    public PusherEvent(String event, String channel, String userId, String data) {
        jsonObject.addProperty("event", event);
        jsonObject.addProperty("channel", channel);
        jsonObject.addProperty("user_id", userId);
        jsonObject.addProperty("data", data);
    }

    public PusherEvent(String event, String channel, String userId, Map<String, Object> data) {
        this(event, channel, userId, new Gson().toJson(data));
    }

    public PusherEvent(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public String toJson() {
        final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(jsonObject);
    }

    public static PusherEvent fromJson(String json) {
        final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return new PusherEvent(gson.fromJson(json, JsonObject.class));
    }
}
