package com.pusher.client.crypto.nacl;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.copyOf;

import com.pusher.client.util.internal.Base64;
import org.junit.Before;
import org.junit.Test;

public class SecretBoxOpenerTest {

    byte[] key = Base64.decode("6071zp2l/GPnDPDXNWTJDHyIZ8pZMvQrYsa4xuTKK2c=");

    byte[] nonce = Base64.decode("xsbOS0KylAV2ziTDHrP/7rSFqpCOah3p");
    byte[] cipher = Base64.decode("tvttPE2PRQp0bWDmaPyiEU8YJGztmTvTN77OoPwftTNTdDgJXwxHQPE=");

    SecretBoxOpener subject;

    @Before
    public void setUp() {
        subject = new SecretBoxOpener(key);
    }

    @Test
    public void open() {
        byte[] clearText = subject.open(cipher, nonce);

        assertThat(new String(clearText)).isEqualTo("{\"message\":\"hello world\"}");
    }

    @Test(expected = AuthenticityException.class)
    public void openFailsForTamperedCipher() {
        byte[] tamperedCipher = copyOf(cipher, cipher.length);
        tamperedCipher[0] ^= tamperedCipher[0];

        subject.open(tamperedCipher, nonce);
    }

    @Test(expected = NullPointerException.class)
    public void openFailsAfterClearKey() {
        subject.clearKey();

        subject.open(cipher, nonce);
    }
}