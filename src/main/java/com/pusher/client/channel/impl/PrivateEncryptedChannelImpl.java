package com.pusher.client.channel.impl;

import com.google.gson.Gson;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.PrivateEncryptedChannel;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.crypto.nacl.SecretBoxOpener;
import com.pusher.client.util.Base64;
import com.pusher.client.util.Factory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class PrivateEncryptedChannelImpl extends ChannelImpl implements PrivateEncryptedChannel {

    private static final Gson GSON = new Gson();
    private final InternalConnection connection;
    private final Authorizer authorizer;
    private SecretBoxOpener secretBoxOpener;

    private class PrivateEncryptedChannelData {
        byte[] auth;
        final String channelData;

        protected PrivateEncryptedChannelData(String auth, String channelData) {
            this.auth = auth.getBytes();
            this.channelData = channelData;
        }

        public String getAuth() {
            return new String(auth);
        }

        public String getChannelData() {
            return channelData;
        }

        protected void clearAuthToken(){
            Arrays.fill(auth, (byte)0);
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

    private void saveSharedSecret(String sharedSecret) {
        secretBoxOpener = new SecretBoxOpener(Base64.decode(sharedSecret));

        // todo can we clear everything when the user disconnects totally?
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
                        "from the Authorizer, expected an auth token and shared_secret but got: " + authResponse);
            } else {
                authorizerData = new PrivateEncryptedChannelData(authKey, channelData);
                saveSharedSecret(sharedSecret);
            }

        } catch (final AuthorizationFailureException e) {
            throw e; // pass this upwards
        } catch (final Exception e) {
            // any other errors need to be captured properly and passed upwards
            throw new AuthorizationFailureException("Unable to parse response from Authorizer: " + authResponse, e);
        }
    }

    protected void tearDownChannel() {
        secretBoxOpener.clearKey();
        authorizerData.clearAuthToken();
    }

    @Override
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
