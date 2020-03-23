package com.pusher.client.channel;

/**
 * Interface to listen to private encrypted channel events.
 */
public interface PrivateEncryptedChannelEventListener extends ChannelEventListener {
    /**
     * Called when an attempt to authenticate a private encrypted channel fails.
     *
     * @param message
     *            A description of the problem.
     * @param e
     *            An associated exception, if available.
     */
    void onAuthenticationFailure(String message, Exception e);

    // todo: handle not receiving the shared secret?
}
