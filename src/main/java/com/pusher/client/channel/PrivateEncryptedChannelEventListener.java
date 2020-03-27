package com.pusher.client.channel;

/**
 * Interface to listen to private encrypted channel events.
 * Note: This needs to extend the PrivateChannelEventListener because of the ChannelManager clearDownSubscription
 */
public interface PrivateEncryptedChannelEventListener extends PrivateChannelEventListener {

    //  TODO: add onDecryptionFailure
}
