package com.pusher.client.channel;

import com.google.gson.*;

import java.lang.reflect.Type;

public class PusherEventDeserializer implements JsonDeserializer<PusherEvent> {
    @Override
    public PusherEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        return new PusherEvent(json.getAsJsonObject());
    }

}
