package com.pusher.client.channel.impl;

import com.google.gson.Gson;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.PrivateEncryptedChannel;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class PrivateEncryptedChannelImpl extends ChannelImpl implements PrivateEncryptedChannel {

    private static final Gson GSON = new Gson();
    private final InternalConnection connection;
    private final Authorizer authorizer;

    private class PrivateEncryptedChannelData {
        final String auth;
        final String sharedSecret;
        final String channelData;

        protected PrivateEncryptedChannelData(String auth, String sharedSecret, String channelData) {
            this.auth = auth;
            this.sharedSecret = sharedSecret;
            this.channelData = channelData;
        }

        public String getAuth() {
            return auth;
        }

        public String getSharedSecret() {
            return sharedSecret;
        }

        public String getChannelData() {
            return channelData;
        }
    }

    private PrivateEncryptedChannelData authorizerData;

    public PrivateEncryptedChannelImpl(final InternalConnection connection,
                                       final String channelName,
                                       final Authorizer authorizer,
                                       final Factory factory) {
        super(channelName, factory);
        this.connection = connection;
        this.authorizer = authorizer;
    }

    /* PrivateChannel implementation */


    /* Base class overrides */

    @Override
    public void bind(final String eventName, final SubscriptionEventListener listener) {

        if (!(listener instanceof PrivateEncryptedChannelEventListener)) {
            throw new IllegalArgumentException(
                    "Only instances of PrivateEncryptedChannelEventListener can be bound " +
                            "to a private encrypted channel");
        }

        super.bind(eventName, listener);
    }

    private void saveSharedSecret() {
        final String authResponse = getAuthResponse();
        final Map authResponseMap = GSON.fromJson(authResponse, Map.class);
        final String sharedSecret = (String)authResponseMap.get("shared_secret");
        final byte[] sharedSecretBase64 = Base64.getDecoder().decode(sharedSecret);

        // todo set up secret box opener -> pass the key
        // todo make sure when unsubscribe we clear the text
        // todo can we clear everything when the user disconnects totally
    }

    /**
     * ensure we've got all the bits and pieces we need to continue
     */
    protected void prepareChannel() {
        final String authResponse = getAuthResponse();

        try {
            final Map authResponseMap = GSON.fromJson(authResponse, Map.class);
            final String authKey = (String) authResponseMap.get("auth");
            final String sharedSecret = (String) authResponseMap.get("shared_secret");
            final String channelData = (String)authResponseMap.get("channel_data");

            if (authKey == null || sharedSecret == null) {
                throw new AuthorizationFailureException("Didn't receive all the fields we expected " +
                        "from the Authorizer: " + authResponse);
            }

            authorizerData = new PrivateEncryptedChannelData(authKey, sharedSecret, channelData);
            saveSharedSecret();

        } catch (final Exception e) {
            throw new AuthorizationFailureException("Unable to parse response from Authorizer: " + authResponse, e);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String toSubscribeMessage() {

        // create the data part
        final Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
        dataMap.put("channel", name);
        dataMap.put("auth", authorizerData.getAuth());
        if (authorizerData.getChannelData() != null) {
            dataMap.put("channel_data", authorizerData.channelData);
        }

        // create the wrapper part
        final Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
        jsonObject.put("event", "pusher:subscribe");
        jsonObject.put("authorizerData", dataMap);
        jsonObject.put("data", dataMap);

        return GSON.toJson(jsonObject);
    }

    private String getAuthResponse() {
        final String socketId = connection.getSocketId();
        return authorizer.authorize(getName(), socketId);
    }

    @Override
    protected String[] getDisallowedNameExpressions() {
        return new String[] { "^(?!private-encrypted).*" };
    }

    @Override
    public String toString() {
        return String.format("[Private Encrypted Channel: name=%s]", name);
    }
}
