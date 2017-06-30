package com.pusher.client.channel;

/**
 * Interface to listen to private channel events.
 */
public interface PrivateChannelEventListener extends ChannelEventListener {
    /**
     * Called when an attempt to authenticate a private channel fails.
     *
     * @param message
     *            A description of the problem.
     * @param e
     *            An associated exception, if available.
     */
    void onAuthenticationFailure(String message, Exception e);
}
