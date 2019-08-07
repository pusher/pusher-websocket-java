package com.pusher.client.channel;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class PusherEventDeserializer implements JsonDeserializer<PusherEvent> {
    private final Gson GSON = new Gson();

    @Override
    @SuppressWarnings("unchecked")
    public PusherEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new PusherEvent(GSON.fromJson(json, Map.class));
    }
}
