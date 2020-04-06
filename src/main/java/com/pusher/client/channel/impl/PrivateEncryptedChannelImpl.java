package com.pusher.client.channel.impl;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PrivateEncryptedChannel;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.crypto.nacl.AuthenticityException;
import com.pusher.client.crypto.nacl.SecretBoxOpener;
import com.pusher.client.crypto.nacl.SecretBoxOpenerFactory;
import com.pusher.client.util.Factory;
import com.pusher.client.util.internal.Base64;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PrivateEncryptedChannelImpl extends ChannelImpl implements PrivateEncryptedChannel {

    private final InternalConnection connection;
    private final Authorizer authorizer;
    private SecretBoxOpenerFactory secretBoxOpenerFactory;
    private SecretBoxOpener secretBoxOpener;

    // For not hanging on to shared secret past the Pusher.disconnect() call,
    // i.e. when not necessary. Pusher.connect(...) call will trigger re-subscribe
    // and hence re-authenticate which creates a new secretBoxOpener.
    private ConnectionEventListener disposeSecretBoxOpenerOnDisconnectedListener =
            new ConnectionEventListener() {

        @Override
        public void onConnectionStateChange(ConnectionStateChange change) {
            disposeSecretBoxOpener();
        }

        @Override
        public void onError(String message, String code, Exception e) {
            // nop
        }
    };

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

    @Override
    public String toSubscribeMessage() {
        String authKey = authenticate();

        // create the data part
        final Map<Object, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("channel", name);
        dataMap.put("auth", authKey);

        // create the wrapper part
        final Map<Object, Object> jsonObject = new LinkedHashMap<>();
        jsonObject.put("event", "pusher:subscribe");
        jsonObject.put("data", dataMap);

        return GSON.toJson(jsonObject);
    }

    private String authenticate() {
        try {
            @SuppressWarnings("rawtypes") // anything goes in JS
            final Map authResponse = GSON.fromJson(getAuthResponse(), Map.class);

            final String auth = (String) authResponse.get("auth");
            final String sharedSecret = (String) authResponse.get("shared_secret");

            if (auth == null || sharedSecret == null) {
                throw new AuthorizationFailureException("Didn't receive all the fields expected " +
                        "from the Authorizer, expected an auth and shared_secret.");
            } else {
                createSecretBoxOpener(Base64.decode(sharedSecret));
                return auth;
            }
        } catch (final AuthorizationFailureException e) {
            throw e; // pass this upwards
        } catch (final Exception e) {
            // any other errors need to be captured properly and passed upwards
            throw new AuthorizationFailureException("Unable to parse response from Authorizer", e);
        }
    }

    private void createSecretBoxOpener(byte[] key) {
        secretBoxOpener = secretBoxOpenerFactory.create(key);
        setListenerToDisposeSecretBoxOpenerOnDisconnected();
    }

    private void setListenerToDisposeSecretBoxOpenerOnDisconnected() {
        connection.bind(ConnectionState.DISCONNECTED,
                disposeSecretBoxOpenerOnDisconnectedListener);
    }

    @Override
    public void updateState(ChannelState state) {
        super.updateState(state);

        if (state == ChannelState.UNSUBSCRIBED) {
            disposeSecretBoxOpener();
        }
    }

    @Override
    public PusherEvent prepareEvent(String event, String message) {

        if (secretBoxOpener == null) {
            notifyListenersOfDecryptFailure(event, "Too many failed attempts to decrypt a previous message.");
        } else {

            try {

                Map<String, Object> receivedMessage =
                        GSON.<Map<String, Object>>fromJson(message, Map.class);
                final String decryptedMessage = decryptMessage((String) receivedMessage.get("data"));
                receivedMessage.replace("data", decryptedMessage);

                return new PusherEvent(receivedMessage);

            } catch (AuthenticityException e1) {

                // retry once only.
                disposeSecretBoxOpener();
                authenticate();

                try {
                    Map<String, Object> receivedMessage =
                            GSON.<Map<String, Object>>fromJson(message, Map.class);
                    final String decryptedMessage = decryptMessage((String) receivedMessage.get("data"));
                    receivedMessage.replace("data", decryptedMessage);

                    return new PusherEvent(receivedMessage);
                } catch (AuthenticityException e2) {
                    disposeSecretBoxOpener();
                    notifyListenersOfDecryptFailure(event, "Failed to decrypt message.");
                }
            }

        }
        return null;
    }

    private void notifyListenersOfDecryptFailure(final String event, final String reason) {
        Set<SubscriptionEventListener> listeners = getInterestedListeners(event);
        if (listeners != null) {
            for (SubscriptionEventListener listener : listeners) {
                ((PrivateEncryptedChannelEventListener)listener).onDecryptionFailure(
                        event, reason);
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

    private void disposeSecretBoxOpener() {
        if (secretBoxOpener != null) {
            secretBoxOpener.clearKey();
            secretBoxOpener = null;
            removeListenerToDisposeSecretBoxOpenerOnDisconnected();
        }
    }

    private void removeListenerToDisposeSecretBoxOpenerOnDisconnected() {
        connection.unbind(ConnectionState.DISCONNECTED,
                disposeSecretBoxOpenerOnDisconnectedListener);
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
