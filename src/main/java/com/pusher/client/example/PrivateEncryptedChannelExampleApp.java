package com.pusher.client.example;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateEncryptedChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpChannelAuthorizer;

/*
This app demonstrates how to use Private Encrypted Channels.

Please ensure you update this relevant parts below with your Pusher credentials before running,
and ensure you have set up an authorization endpoint with end to end encryption. Your Pusher credentials
can be found at https://dashboard.pusher.com, selecting the channels project, and visiting the App Keys
tab.

A demonstration authorization endpoint using nodejs can be found
https://github.com/pusher/pusher-channels-auth-example#using-e2e-encryption

For more information on private encrypted channels please read
https://pusher.com/docs/channels/using_channels/encrypted-channels

For more specific information on how to use private encrypted channels check out
https://github.com/pusher/pusher-websocket-java#private-encrypted-channels
 */

public class PrivateEncryptedChannelExampleApp {

    // make sure the following variables are configured for your instance:
    private String channelsKey = "FILL_ME_IN";
    private String channelName = "private-encrypted-channel";
    private String eventName = "my-event";
    private String cluster = "eu";
    private final String channelAuthorizationEndpoint =
            "http://localhost:3030/pusher/auth";

    public static void main(final String[] args) {
        new PrivateEncryptedChannelExampleApp(args);
    }

    private PrivateEncryptedChannelExampleApp(final String[] args) {
        // if using from the command line, these variables need to be passed
        switch (args.length) {
            case 4:
                cluster = args[3];
            case 3:
                eventName = args[2];
            case 2:
                channelName = args[1];
            case 1:
                channelsKey = args[0];
        }

        // create a HttpChannelAuthorizer that points to your channel authorization server
        final HttpChannelAuthorizer channelAuthorizer = new HttpChannelAuthorizer(
                channelAuthorizationEndpoint
        );

        // configure your Pusher connection with the options you want
        final PusherOptions options = new PusherOptions()
                .setCluster(cluster)
                .setChannelAuthorizer(channelAuthorizer)
                .setUseTLS(true);
        Pusher pusher = new Pusher(channelsKey, options);

        // set up a ConnectionEventListener to listen for connection changes to Pusher
        ConnectionEventListener connectionEventListener = new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                System.out.printf(
                        "Connection state changed from [%s] to [%s]%n",
                        change.getPreviousState(),
                        change.getCurrentState()
                );
            }

            @Override
            public void onError(String message, String code, Exception e) {
                System.out.printf(
                        "An error was received with message [%s], code [%s], exception [%s]%n",
                        message,
                        code,
                        e
                );
            }
        };

        // connect to Pusher
        pusher.connect(connectionEventListener);

        // set up a PrivateEncryptedChannelEventListener to listen for messages to the channel and event we are interested in
        PrivateEncryptedChannelEventListener privateEncryptedChannelEventListener = new PrivateEncryptedChannelEventListener() {
            @Override
            public void onSubscriptionSucceeded(String channelName) {
                System.out.printf("Subscription to channel [%s] succeeded%n", channelName);
            }

            @Override
            public void onEvent(PusherEvent event) {
                System.out.printf("Received event [%s]%n", event.toString());
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                System.out.printf(
                        "Authorization failure due to [%s], exception was [%s]%n",
                        message,
                        e
                );
            }

            @Override
            public void onDecryptionFailure(String event, String reason) {
                System.out.printf(
                        "An error was received decrypting message for event:[%s] - reason: [%s]%n",
                        event,
                        reason
                );
            }
        };

        pusher.subscribePrivateEncrypted(
                channelName,
                privateEncryptedChannelEventListener,
                eventName
        );

        // Keep main thread asleep while we watch for events or application will terminate
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
