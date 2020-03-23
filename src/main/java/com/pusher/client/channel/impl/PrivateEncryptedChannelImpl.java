package com.pusher.client.channel.impl;

import com.google.gson.Gson;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.PrivateEncryptedChannel;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

import java.util.LinkedHashMap;
import java.util.Map;

public class PrivateEncryptedChannelImpl extends ChannelImpl implements PrivateEncryptedChannel {

    private static final Gson GSON = new Gson();
    private final InternalConnection connection;
    private final Authorizer authorizer;

    protected String channelData;

    public PrivateEncryptedChannelImpl(final InternalConnection connection, final String channelName,
                                       final Authorizer authorizer, final Factory factory) {
        super(channelName, factory);
        this.connection = connection;
        this.authorizer = authorizer;
    }

    /* PrivateChannel implementation */


    /* Base class overrides */

    @Override
    public void bind(final String eventName, final SubscriptionEventListener listener) {

        if (listener instanceof PrivateEncryptedChannelEventListener == false) {
            throw new IllegalArgumentException(
                    "Only instances of PrivateChannelEventListener can be bound to a private channel");
        }

        super.bind(eventName, listener);
    }

    // todo: is this the best way of doing this? Should each channel have a prepare/validate
    // requirements before we send a subscribe event where we can do this work.
    protected void saveSharedSecret() {
        final String authResponse = getAuthResponse();
        final Map authResponseMap = GSON.fromJson(authResponse, Map.class);
        final String sharedSecret = (String)authResponseMap.get("shared_secret");

        // todo actually save this somewhere
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String toSubscribeMessage() {

        final String authResponse = getAuthResponse();

        try {
            final Map authResponseMap = GSON.fromJson(authResponse, Map.class);
            final String authKey = (String)authResponseMap.get("auth");
            channelData = (String)authResponseMap.get("channel_data");

            final Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
            jsonObject.put("event", "pusher:subscribe");

            final Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
            dataMap.put("channel", name);
            dataMap.put("auth", authKey);
            if (channelData != null) {
                dataMap.put("channel_data", channelData);
            }

            jsonObject.put("data", dataMap);

            final String json = GSON.toJson(jsonObject);
            return json;
        }
        catch (final Exception e) {
            throw new AuthorizationFailureException("Unable to parse response from Authorizer: " + authResponse, e);
        }
    }

    private String getAuthResponse() {
        final String socketId = connection.getSocketId();
        return authorizer.authorize(getName(), socketId);
    }

    // todo: does this need to change to say private-encrypted?
    @Override
    protected String[] getDisallowedNameExpressions() {
        return new String[] { "^(?!private-).*" };
    }

    @Override
    public String toString() {
        return String.format("[Private Encrypted Channel: name=%s]", name);
    }
}
