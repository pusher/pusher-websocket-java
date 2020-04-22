package com.pusher.client.example;

import java.util.Map;

import com.google.gson.Gson;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;

/*
This app demonstrates how to use a standard Pusher channel.

Please ensure you update this relevant parts below with your Pusher credentials before running.
Your Pusher credentials can be found at https://dashboard.pusher.com, selecting the channels project,
and visiting the App Keys tab.

For more specific information on how to use channels check out
https://pusher.com/docs/channels/using_channels/channels
*/

public class ExampleApp {

    // make sure the following variables are configured for your instance:
    private String channelsKey = "FILL_ME_IN";
    private String channelName = "my-channel";
    private String eventName = "my-event";
    private String cluster = "eu";

    public static void main(final String[] args) {
        new ExampleApp(args);
    }

    public ExampleApp(final String[] args) {

        // if using from the command line, these variables need to be passed
        switch (args.length) {
            case 4: cluster = args[3];
            case 3: eventName = args[2];
            case 2: channelName = args[1];
            case 1: channelsKey = args[0];
        }

        // configure your Pusher connection with the options you want
        final PusherOptions options = new PusherOptions()
                .setUseTLS(true)
                .setCluster(cluster);
        Pusher pusher = new Pusher(channelsKey, options);

        // set up a ConnectionEventListener to listen for connection changes to Pusher
        ConnectionEventListener connectionEventListener = new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                System.out.println(String.format("Connection state changed from [%s] to [%s]",
                        change.getPreviousState(), change.getCurrentState()));
            }

            @Override
            public void onError(String message, String code, Exception e) {
                System.out.println(String.format("An error was received with message [%s], code [%s], exception [%s]",
                        message, code, e));
            }
        };

        // connect to Pusher
        pusher.connect(connectionEventListener);

        // set up a ChannelEventListener to listen for messages to the channel and event we are interested in
        ChannelEventListener channelEventListener = new ChannelEventListener() {
            @Override
            public void onSubscriptionSucceeded(String channelName) {
                System.out.println(String.format(
                        "Subscription to channel [%s] succeeded", channelName));
            }

            @Override
            public void onEvent(PusherEvent event) {
                System.out.println(String.format(
                        "Received event [%s]", event.toString()));
            }
        };

        // subscribe to the channel and with the event listener for the event name
        Channel channel = pusher.subscribe(channelName, channelEventListener, eventName);


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
}
