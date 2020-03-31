package com.pusher.client.channel.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PrivateEncryptedChannel;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.crypto.nacl.SecretBoxOpener;
import com.pusher.client.util.Factory;
import com.pusher.client.util.internal.Base64;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class PrivateEncryptedChannelImpl extends ChannelImpl implements PrivateEncryptedChannel {

    private final InternalConnection connection;
    private final Authorizer authorizer;
    protected SecretBoxOpener secretBoxOpener;

    public PrivateEncryptedChannelImpl(final InternalConnection connection,
                                       final String channelName,
                                       final Authorizer authorizer,
                                       final Factory factory) {
        super(channelName, factory);
        this.connection = connection;
        this.authorizer = authorizer;
    }

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

    private class AuthResponse {
        char[] auth;
        @SerializedName("shared_secret")
        char[] sharedSecret;

        public char[] getAuth() {
            return auth;
        }

        public char[] getSharedSecret() {
            return sharedSecret;
        }
    }

    //
    public class GsonHelper {
        public final Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
                new ByteArrayToBase64TypeAdapter()).create();

        // Using Android's base64 libraries. This can be replaced with any base64 library.
        private class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
            public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return Base64.decode(json.getAsString());
            }

            public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
                throw new UnsupportedOperationException("");
            }
        }
    }

    protected byte[] checkAuthentication() {

        try {
            final AuthResponse authResponse = GSON.fromJson(getAuthResponse(), AuthResponse.class);
            final char[] authKey = authResponse.getAuth();
            final char[] sharedSecret = authResponse.getSharedSecret();

            if (authKey == null || sharedSecret.length > 0) {
                throw new AuthorizationFailureException("Didn't receive all the fields expected " +
                        "from the Authorizer, expected an auth token and shared_secret.");
            } else {
                secretBoxOpener = new SecretBoxOpener(Base64.decode(new String(sharedSecret)));
                return (new String(authKey).getBytes());
            }

        } catch (final AuthorizationFailureException e) {
            throw e; // pass this upwards
        } catch (final Exception e) {
            // any other errors need to be captured properly and passed upwards
            throw new AuthorizationFailureException("Unable to parse response from Authorizer", e);
        }
    }

    @Override
    public String toSubscribeMessage() {

        byte[] authKey = checkAuthentication();

        // create the data part
        final Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
        dataMap.put("channel", name);
        dataMap.put("auth", new String(authKey));

        // create the wrapper part
        final Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
        jsonObject.put("event", "pusher:subscribe");
        jsonObject.put("data", dataMap);

        return GSON.toJson(jsonObject);
    }

    @Override
    public void updateState(ChannelState state) {
        super.updateState(state);

        if (state == ChannelState.UNSUBSCRIBED) {
            tearDownChannel();
        }
    }

    private void tearDownChannel() {
        if (secretBoxOpener != null) {
            secretBoxOpener.clearKey();
            secretBoxOpener = null;
        }
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
