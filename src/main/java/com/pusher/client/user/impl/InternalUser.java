package com.pusher.client.user.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pusher.client.AuthenticationFailureException;
import com.pusher.client.UserAuthenticator;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.channel.impl.ChannelManager;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.user.User;
import com.pusher.client.user.impl.message.AuthenticationResponse;
import com.pusher.client.user.impl.message.SigninMessage;
import com.pusher.client.util.Factory;

import java.util.Map;
import java.util.logging.Logger;

public class InternalUser implements User {

    private static final Gson GSON = new Gson();
    private static final Logger log = Logger.getLogger(User.class.getName());

    private static class ConnectionStateChangeHandler implements ConnectionEventListener {

        private final InternalUser user;

        public ConnectionStateChangeHandler(InternalUser user) {
            this.user = user;
        }

        @Override
        public void onConnectionStateChange(ConnectionStateChange change) {
            switch (change.getCurrentState()) {
                case CONNECTED:
                    user.attemptSignin();
                    break;
                case CONNECTING:
                case DISCONNECTED:
                    user.disconnect();
                    break;
                default:
                    // NOOP
            }
        }

        @Override
        public void onError(String message, String code, Exception e) {
            log.warning(message);
        }
    }

    private final InternalConnection connection;
    private final UserAuthenticator userAuthenticator;
    private final ChannelManager channelManager;
    private boolean signinRequested;
    private final ServerToUserChannel serverToUserChannel;
    private String userId;

    public InternalUser(InternalConnection connection, UserAuthenticator userAuthenticator, Factory factory) {
        this.connection = connection;
        this.userAuthenticator = userAuthenticator;
        this.channelManager = factory.getChannelManager();
        this.signinRequested = false;
        this.serverToUserChannel = new ServerToUserChannel(this, factory);

        connection.bind(ConnectionState.ALL, new ConnectionStateChangeHandler(this));
    }

    public void signin() throws AuthenticationFailureException {
        if (signinRequested || userId != null) {
            return;
        }

        signinRequested = true;
        attemptSignin();
    }

    public void handleEvent(PusherEvent event) {
        if (event.getEventName().equals("pusher:signin_success")) {
            onSigninSuccess(event);
        }
    }

    private void attemptSignin() throws AuthenticationFailureException {
        if (!signinRequested || userId != null) {
            return;
        }

        if (connection.getState() != ConnectionState.CONNECTED) {
            // Signin will be attempted when the connection is connected
            return;
        }

        AuthenticationResponse authenticationResponse = getAuthenticationResponse();
        connection.sendMessage(authenticationResponseToSigninMessage(authenticationResponse));
    }

    private static String authenticationResponseToSigninMessage(AuthenticationResponse authenticationResponse) {
        return GSON.toJson(new SigninMessage(authenticationResponse.getAuth(), authenticationResponse.getUserData()));
    }

    private AuthenticationResponse getAuthenticationResponse() throws AuthenticationFailureException {
        String response = userAuthenticator.authenticate(connection.getSocketId());
        try {
            AuthenticationResponse authenticationResponse = GSON.fromJson(response, AuthenticationResponse.class);
            if (authenticationResponse.getAuth() == null || authenticationResponse.getUserData() == null) {
                throw new AuthenticationFailureException(
                        "Didn't receive all the fields expected from the UserAuthenticator. Expected auth and user_data"
                );
            }
            return authenticationResponse;
        } catch (JsonSyntaxException e) {
            throw new AuthenticationFailureException("Unable to parse response from AuthenticationResponse");
        }
    }

    private void onSigninSuccess(PusherEvent event) {
        try {
            String userData = (String) GSON.fromJson(event.getData(), Map.class).get("user_data");
            userId = (String) GSON.fromJson(userData, Map.class).get("id");
        } catch (Exception e) {
            log.severe("Failed parsing user data after signin");
            return;
        }

        if (userId == null) {
            log.severe("User data doesn't contain an id");
            return;
        }
        channelManager.subscribeTo(serverToUserChannel, null);
    }

    private void disconnect() {
        if (serverToUserChannel.isSubscribed()) {
            channelManager.unsubscribeFrom(serverToUserChannel.getName());
        }
        userId = null;
    }

    @Override
    public String userId() {
        return userId;
    }

    @Override
    public void bind(String eventName, SubscriptionEventListener listener) {
        serverToUserChannel.bind(eventName, listener);
    }

    @Override
    public void bindGlobal(SubscriptionEventListener listener) {
        serverToUserChannel.bindGlobal(listener);
    }

    @Override
    public void unbind(String eventName, SubscriptionEventListener listener) {
        serverToUserChannel.unbind(eventName, listener);
    }

    @Override
    public void unbindGlobal(SubscriptionEventListener listener) {
        serverToUserChannel.unbindGlobal(listener);
    }
}
