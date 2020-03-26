package com.pusher.client.util;

import static com.google.common.truth.Truth.assertThat;

import com.pusher.client.util.internal.Base64;
import org.junit.Test;

public class Base64Test {

    @Test
    public void decodeValidChars() {
        String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        assertThat(Base64.decode(validChars)).isNotEmpty();
    }

    // https://en.wikipedia.org/wiki/Base64#URL_applications
    @Test(expected = IllegalArgumentException.class)
    public void failDecodingMinusChar() {
        Base64.decode("-");
    }

    // https://en.wikipedia.org/wiki/Base64#URL_applications
    @Test(expected = IllegalArgumentException.class)
    public void failDecodingUnderscoreChar() {
        Base64.decode("_");
    }

}
