package com.pusher.client.channel;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PusherEventDeserializer implements JsonDeserializer<PusherEvent> {
    @Override
    public PusherEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> eventData = new HashMap<String, Object>();
        JsonObject jsonObject = json.getAsJsonObject();
        Set jsonEntrySet = jsonObject.entrySet();
        for (Object key : jsonEntrySet) {
            eventData.put(key.toString(), jsonObject.get(key.toString()));
        }
        return new PusherEvent(eventData);
    }
}
