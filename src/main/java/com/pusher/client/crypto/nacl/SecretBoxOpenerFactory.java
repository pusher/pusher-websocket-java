package com.pusher.client.crypto.nacl;

public class SecretBoxOpenerFactory {

     public SecretBoxOpener create(byte[] key) {
        return new SecretBoxOpener(key);
    }
}
