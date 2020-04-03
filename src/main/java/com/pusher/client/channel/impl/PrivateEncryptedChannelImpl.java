package com.pusher.client.channel.impl;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PrivateEncryptedChannel;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.crypto.nacl.SecretBoxOpener;
import com.pusher.client.crypto.nacl.SecretBoxOpenerFactory;
import com.pusher.client.util.Factory;
import com.pusher.client.util.internal.Base64;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PrivateEncryptedChannelImpl extends ChannelImpl implements PrivateEncryptedChannel {

    private final InternalConnection connection;
    private final Authorizer authorizer;
    private SecretBoxOpenerFactory secretBoxOpenerFactory;
    private SecretBoxOpener secretBoxOpener;

    public PrivateEncryptedChannelImpl(final InternalConnection connection,
                                       final String channelName,
                                       final Authorizer authorizer,
                                       final Factory factory,
                                       final SecretBoxOpenerFactory secretBoxOpenerFactory) {
        super(channelName, factory);
        this.connection = connection;
        this.authorizer = authorizer;
        this.secretBoxOpenerFactory = secretBoxOpenerFactory;
    }

    @Override
    public void bind(final String eventName, final SubscriptionEventListener listener) {

        if (!(listener instanceof PrivateEncryptedChannelEventListener)) {
            throw new IllegalArgumentException(
                    "Only instances of PrivateEncryptedChannelEventListener can be bound " +
                            "to a private encrypted channel");
        }

        super.bind(eventName, listener);
    }

    private String authenticate() {

        try {
            final Map authResponseMap = GSON.fromJson(getAuthResponse(), Map.class);
            final String auth = (String) authResponseMap.get("auth");
            final String sharedSecret = (String) authResponseMap.get("shared_secret");

            if (auth == null || sharedSecret == null) {
                throw new AuthorizationFailureException("Didn't receive all the fields expected " +
                        "from the Authorizer, expected an auth and shared_secret.");
            } else {
                secretBoxOpener = secretBoxOpenerFactory.create(
                        Base64.decode(sharedSecret));
                return auth;
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

        String authKey = authenticate();

        // create the data part
        final Map<Object, Object> dataMap = new LinkedHashMap<Object, Object>();
        dataMap.put("channel", name);
        dataMap.put("auth", authKey);

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
        }
    }

    private class ReceivedMessage {
        private String data;

        public String getData() {
            return data;
        }
    }

    @Override
    public void onMessage(String event, String message) {

        if (event.equals(SUBSCRIPTION_SUCCESS_EVENT)) {
            updateState(ChannelState.SUBSCRIBED);
        } else {

            final Set<SubscriptionEventListener> listeners;
            synchronized (lock) {
                final Set<SubscriptionEventListener> sharedListeners = eventNameToListenerMap.get(event);
                if (sharedListeners != null) {
                    listeners = new HashSet<>(sharedListeners);
                } else {
                    listeners = null;
                }
            }

            if (listeners != null) {
                try {
                    // get the data part to decrypt
                    ReceivedMessage receivedMessage = GSON.fromJson(message, ReceivedMessage.class);
                    final String decryptedMessage = decryptMessage(receivedMessage.getData());

                    // wrap it up as a PusherEvent to send to listeners
                    PusherEvent pusherEventModified = GSON.fromJson(
                            message, PusherEvent.class);
                    pusherEventModified.setDecryptedData(decryptedMessage);

                    // notify all the listeners
                    for (final SubscriptionEventListener listener : listeners) {
                        factory.queueOnEventThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onEvent(pusherEventModified);
                            }
                        });
                    }

                } catch (Exception exception) {
                    for (final SubscriptionEventListener listener : listeners) {
                        factory.queueOnEventThread(new Runnable() {
                            @Override
                            public void run() {
                                ((PrivateEncryptedChannelEventListener)listener)
                                        .onDecryptionFailure(exception);
                            }
                        });
                    }
                }

            }
        }
    }

    private class EncryptedReceivedData {
        String nonce;
        String ciphertext;

        public byte[] getNonce() {
            return Base64.decode(nonce);
        }

        public byte[] getCiphertext() {
            return Base64.decode(ciphertext);
        }
    }

    private String decryptMessage(String data) {

        final EncryptedReceivedData encryptedReceivedData =
                GSON.fromJson(data, EncryptedReceivedData.class);

        return new String(secretBoxOpener.open(
                encryptedReceivedData.getCiphertext(),
                encryptedReceivedData.getNonce()));
    }

    private String getAuthResponse() {
        final String socketId = connection.getSocketId();
        return authorizer.authorize(getName(), socketId);
    }

    @Override
    protected String[] getDisallowedNameExpressions() {
        return new String[] { "^(?!private-encrypted-).*" };
    }

    @Override
    public String toString() {
        return String.format("[Private Encrypted Channel: name=%s]", name);
    }
}
