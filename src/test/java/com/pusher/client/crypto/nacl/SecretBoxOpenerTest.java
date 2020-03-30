package com.pusher.client.crypto.nacl;

import static com.google.common.truth.Truth.assertThat;

import com.pusher.client.util.internal.Base64;
import org.junit.Before;
import org.junit.Test;

public class SecretBoxOpenerTest {

    byte[] key = Base64.decode("6071zp2l/GPnDPDXNWTJDHyIZ8pZMvQrYsa4xuTKK2c=");
    SecretBoxOpener subject;

    @Before
    public void setUp() {
        subject = new SecretBoxOpener(key);
    }

    @Test
    public void open() {
        byte[] cipher = Base64.decode("tvttPE2PRQp0bWDmaPyiEU8YJGztmTvTN77OoPwftTNTdDgJXwxHQPE=");
        byte[] nonce = Base64.decode("xsbOS0KylAV2ziTDHrP/7rSFqpCOah3p");

        byte[] clearText = subject.open(cipher, nonce);

        assertThat(new String(clearText)).isEqualTo("{\"message\":\"hello world\"}");
    }
}