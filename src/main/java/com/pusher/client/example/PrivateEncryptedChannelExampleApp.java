package com.pusher.client.example;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateEncryptedChannel;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

/*
This app demonstrates how to use Private Encrypted Channels.

Please ensure you update this relevant parts below with your Pusher credentials before running.
and ensure you have set up an authorization endpoint with end to end encryption. Your Pusher credentials
can be found at https://dashboard.pusher.com, selecting the channels project, and visiting the App Keys
tab.

A demonstration authorization endpoint using nodejs can be found
https://github.com/pusher/pusher-channels-auth-example#using-e2e-encryption

For more information on private encrypted channels please read
https://pusher.com/docs/channels/using_channels/encrypted-channels

For more pecific information on how to use private encrypted channels check out
https://github.com/pusher/pusher-websocket-java#private-encrypted-channels
 */

public class PrivateEncryptedChannelExampleApp implements
        ConnectionEventListener, PrivateEncryptedChannelEventListener {

    private String channelsKey = "FILL_ME_IN";
    private String channelName = "private-encrypted-channel";
    private String eventName = "my-event";
    private String cluster = "eu";
    private String authorizationEndpoint = "http://localhost:3030/pusher/auth";

    private PrivateEncryptedChannel channel;

    public static void main(final String[] args) {
        new PrivateEncryptedChannelExampleApp(args);
    }

    private PrivateEncryptedChannelExampleApp(final String[] args) {
        switch (args.length) {
            case 4: cluster = args[3];
            case 3: eventName = args[2];
            case 2: channelName = args[1];
            case 1: channelsKey = args[0];
        }

        final HttpAuthorizer authorizer = new HttpAuthorizer(
                authorizationEndpoint);
        final PusherOptions options = new PusherOptions().setAuthorizer(authorizer).setEncrypted(true);
        options.setCluster(cluster);

        Pusher pusher = new Pusher(channelsKey, options);
        pusher.connect(this);

        channel = pusher.subscribePrivateEncrypted(channelName, this, eventName);

        // Keep main thread asleep while we watch for events or application will terminate
        while (true) {
            try {
                Thread.sleep(1000);
            }
            catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAuthenticationFailure(String message, Exception e) {
        System.out.println(String.format(
                "Authentication failure due to [%s], exception was [%s]", message, e));
    }

    @Override
    public void onSubscriptionSucceeded(String channelName) {
        System.out.println(String.format(
                "Subscription to channel [%s] succeeded", channel.getName()));
    }

    @Override
    public void onEvent(PusherEvent event) {
        System.out.println(String.format(
                "Received event [%s]", event.toString()));
    }

    @Override
    public void onConnectionStateChange(ConnectionStateChange change) {
        System.out.println(String.format(
                "Connection state changed from [%s] to [%s]",
                change.getPreviousState(),
                change.getCurrentState()));
    }

    @Override
    public void onError(String message, String code, Exception e) {
        System.out.println(String.format(
                "An error was received with message [%s], code [%s], exception [%s]",
                message,
                code,
                e));
    }

    @Override
    public void onDecryptionFailure(String event, String reason) {
        System.out.println(String.format(
                "An error was received decrypting message for event:[%s] - reason: [%s]", event, reason));
    }
}
