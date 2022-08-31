package com.pusher.client.crypto.nacl;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.copyOf;

import com.google.common.collect.Lists;
import com.pusher.client.util.internal.Base64;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class SecretBoxOpenerTest {

    byte[] key = Base64.decode("6071zp2l/GPnDPDXNWTJDHyIZ8pZMvQrYsa4xuTKK2c=");

    byte[] cipher = Base64.decode(
            "tvttPE2PRQp0bWDmaPyiEU8YJGztmTvTN77OoPwftTNTdDgJXwxHQPE="
    );
    byte[] nonce = Base64.decode("xsbOS0KylAV2ziTDHrP/7rSFqpCOah3p");

    SecretBoxOpener subject;

    @Before
    public void setUp() {
        subject = new SecretBoxOpener(key);
    }

    @Test
    public void open() {
        String clearText = subject.open(cipher, nonce);

        assertThat(clearText).isEqualTo("{\"message\":\"hello world\"}");
    }

    @Test
    public void openEmptyMessage() {
        subject =
                new SecretBoxOpener(
                        Base64.decode("dwXDg1sGnypM44uPh5Rts/JIP2Y7XkHR5lB/o3rBlVs=")
                );

        ArrayList<String> nonces = Lists.newArrayList(
                "p8v9RQR5r6o3G7e2KRgteRi5P90ajKVz",
                "ZwJVq1LuvUSL5F2EH7RN8P9BcrkxHJS2",
                "F7ZhEsQLeo/H60T+eghMYdgulNI7iq78",
                "jU9ShtYe8fz/lOUWnxw7hlb+W5cA6neA",
                "yGJN1Q03seO6CS8QHRxDLVvetp0hmyCB",
                "Xi+1aPjEBOTsPATGiEtKnS1YTosvXQQu",
                "mGi+Rj860MfSGFGbYBwjJfK/kdajUFfg",
                "YrIvOUf/n6GELtC9GxkccEJsZWdfDXu6",
                "nRZmojqKgQaPN4KSiIgBfIIk7tU3TuDV"
        );

        ArrayList<String> ciphers = Lists.newArrayList(
                "glAlout7BU/REO+kzS+Ixa6uTnV1v0ZdPdRyV4hn",
                "lbjgrLGKFQJWjaCoTwDV9NxcxSiQ42KdvmPEkN7y",
                "qFpG2oVtc2hJR99wOZeIjdxaZQUJaSxnS631G521",
                "Qi8vqaCBoq0iDXhbOFBBSLO6jLsQlfZ1rJP6W8iZ",
                "QZMiHsoEBGN6GtQQduAMTAdPrspQeLPieCa4+fsH",
                "BHB7z1oJ51+y790myI6U8Iqtu6BK5CGAw0B6RiNA",
                "8oh6WxNppFXRQRxE6YHTmipWR6AOlmZe1oIXjQyY",
                "tfVzEAF0pw7taOmPFWGqfxMHZAYegFr3NHeHQvR3",
                "+lMeOemGsySbPaBuD2G07gZmofHc1XCfaX/NV4Y6"
        );

        for (int i = 0; i < ciphers.size(); i++) {
            String decryptedMessage = subject.open(
                    Base64.decode(ciphers.get(i)),
                    Base64.decode(nonces.get(i))
            );
            assertThat(decryptedMessage).isEqualTo("{\"message\":\"\"}");
        }
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
