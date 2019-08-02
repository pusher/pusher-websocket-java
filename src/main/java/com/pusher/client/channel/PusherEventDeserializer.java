package com.pusher.client.channel;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PusherEventDeserializer implements JsonDeserializer<PusherEvent> {
    @Override
    public PusherEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Gson GSON = new Gson();
        PusherEvent event = new PusherEvent(GSON.fromJson(json, Map.class));
        return event;
    }

}
