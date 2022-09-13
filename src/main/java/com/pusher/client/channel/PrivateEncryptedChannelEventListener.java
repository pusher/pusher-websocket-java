package com.pusher.client.channel;

/**
 * Interface to listen to private encrypted channel events.
 * Note: This needs to extend the PrivateChannelEventListener because in the
 * ChannelManager handleAuthenticationFailure we assume it's safe to cast to a
 * PrivateChannelEventListener
 */
public interface PrivateEncryptedChannelEventListener extends PrivateChannelEventListener {
    void onDecryptionFailure(String event, String reason);
}
