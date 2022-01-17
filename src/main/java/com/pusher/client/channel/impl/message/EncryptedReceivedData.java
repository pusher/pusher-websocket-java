package com.pusher.client.channel.impl.message;

import com.pusher.client.util.internal.Base64;

public class EncryptedReceivedData {
    private String nonce;
    private String ciphertext;

    public byte[] getNonce() {
        return Base64.decode(nonce);
    }

    public byte[] getCiphertext() {
        return Base64.decode(ciphertext);
    }
}

