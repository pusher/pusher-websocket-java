package com.pusher.client.example;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

public class PrivateChannelExampleApp implements ConnectionEventListener, PrivateChannelEventListener {

    private final Pusher pusher;
    private final String channelName;
    private final String eventName;

    private final PrivateChannel channel;

    public static void main(final String[] args) {
        new PrivateChannelExampleApp(args);
    }

    public PrivateChannelExampleApp(final String[] args) {

        final String apiKey = args.length > 0 ? args[0] : "a87fe72c6f36272aa4b1";
        channelName = args.length > 1 ? args[1] : "private-my-channel";
        eventName = args.length > 2 ? args[2] : "my-event";

        final HttpAuthorizer authorizer = new HttpAuthorizer(
                "http://www.leggetter.co.uk/pusher/pusher-examples/php/authentication/src/private_auth.php");
        final PusherOptions options = new PusherOptions().setAuthorizer(authorizer);

        pusher = new Pusher(apiKey, options);
        pusher.connect(this);

        channel = pusher.subscribePrivate(channelName, this, eventName);

        // Keep main thread asleep while we watch for events or application will
        // terminate
        while (true) {
            try {
                Thread.sleep(1000);
            }
            catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /* ConnectionEventListener implementation */

    @Override
    public void onConnectionStateChange(final ConnectionStateChange change) {

        System.out.println(String.format("Connection state changed from [%s] to [%s]", change.getPreviousState(),
                change.getCurrentState()));
    }

    @Override
    public void onError(final String message, final String code, final Exception e) {

        System.out.println(String.format("An error was received with message [%s], code [%s], exception [%s]", message,
                code, e));
    }

    /* PrivateChannelEventListener implementation */

    @Override
    public void onEvent(final String channelName, final String eventName, final String data) {

        System.out.println(String.format("Received event [%s] on channel [%s] with data [%s]", eventName, channelName,
                data));
    }

    @Override
    public void onSubscriptionSucceeded(final String channelName) {

        System.out.println(String.format("Subscription to channel [%s] succeeded", channel.getName()));

        channel.trigger("client-myEvent", "{\"myName\":\"Bob\"}");
    }

    @Override
    public void onAuthenticationFailure(final String message, final Exception e) {

        System.out.println(String.format("Authentication failure due to [%s], exception was [%s]", message, e));
    }
}
