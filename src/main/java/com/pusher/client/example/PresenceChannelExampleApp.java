package com.pusher.client.example;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.User;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpChannelAuthorizer;

import java.util.Set;

/*
This app demonstrates how to use Presence Channels.

Please ensure you update this relevant parts below with your Pusher credentials before running.
Your Pusher credentials can be found at https://dashboard.pusher.com, selecting the channels project,
and visiting the App Keys tab.

A demonstration authorization endpoint using nodejs can be found
https://github.com/pusher/pusher-channels-auth-example

For more information on private encrypted channels please read
https://pusher.com/docs/channels/using_channels/presence-channels
*/
public class PresenceChannelExampleApp {

    // make sure the following variables are configured for your instance:
    private String channelsKey = "FILL_ME_IN";
    private String channelName = "my-channel";
    private String eventName = "my-event";
    private String cluster = "eu";
    private final String channelAuthorizationEndpoint = "http://localhost:3030/pusher/auth";

    private final PresenceChannel channel;

    public static void main(final String[] args) {
        new PresenceChannelExampleApp(args);
    }

    private PresenceChannelExampleApp(final String[] args) {
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
        final HttpChannelAuthorizer channelAuthorizer = new HttpChannelAuthorizer(channelAuthorizationEndpoint);

        // configure your Pusher connection with the options you want
        final PusherOptions options = new PusherOptions()
                .setUseTLS(true)
                .setCluster(cluster)
                .setChannelAuthorizer(channelAuthorizer);
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
                System.out.printf("An error was received with message [%s], code [%s], exception [%s]%n", message, code, e);
            }
        };

        // connect to Pusher
        pusher.connect(connectionEventListener);

        // set up a PresenceChannelEventListener to listen for messages to the channel and event we are interested in
        PresenceChannelEventListener presenceChannelEventListener = new PresenceChannelEventListener() {
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
                System.out.printf("Authorization failure due to [%s], exception was [%s]%n", message, e);
            }

            @Override
            public void onUsersInformationReceived(String channelName, Set<User> users) {
                System.out.println("Received user information");
                printCurrentlySubscribedUsers();
            }

            @Override
            public void userSubscribed(String channelName, User user) {
                System.out.printf("A new user has joined channel [%s]: %s%n", channelName, user.toString());
                printCurrentlySubscribedUsers();
            }

            @Override
            public void userUnsubscribed(String channelName, User user) {
                System.out.printf("A user has left channel [%s]: %s%n", channelName, user);
                printCurrentlySubscribedUsers();
            }
        };

        // subscribe to the channel and with the event listener for the event name
        channel = pusher.subscribePresence(channelName, presenceChannelEventListener, eventName);

        // Keep main thread asleep while we watch for events or application will terminate
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void printCurrentlySubscribedUsers() {
        final StringBuilder sb = new StringBuilder("Users now subscribed to the channel:");
        for (final User user : channel.getUsers()) {
            sb.append("\n\t");
            sb.append(user.toString());

            if (user.equals(channel.getMe())) {
                sb.append(" (me)");
            }
        }

        System.out.println(sb);
    }
}
