/*
Copyright 2020 Pusher Ltd
Copyright 2015 Eve Freeman

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
IN THE SOFTWARE.
*/

package com.pusher.client.crypto.nacl;

import static com.pusher.client.util.internal.Preconditions.checkArgument;
import static com.pusher.client.util.internal.Preconditions.checkNotNull;
import static java.util.Arrays.fill;

public class SecretBoxOpener {

    private byte[] key;

    public SecretBoxOpener(byte[] key) {
        checkNotNull(key, "null key passed");
        checkArgument(
                key.length == 32,
                "key length must be 32 bytes, but is " + key.length + " bytes"
        );

        this.key = key;
    }

    public String open(byte[] cypher, byte[] nonce) throws AuthenticityException {
        checkNotNull(key, "key has been cleared, create new instance");
        checkArgument(
                nonce.length == 24,
                "nonce length must be 24 bytes, but is " + key.length + " bytes"
        );
        try {
            TweetNaclFast.SecretBox secretBox = new TweetNaclFast.SecretBox(key);
            byte[] result = secretBox.open(cypher, nonce);
            return new String(result);
        } catch (Exception e) {
            throw new AuthenticityException();
        }
    }

    public void clearKey() {
        fill(key, (byte) 0);
        if (key[0] != 0) {
            // so that hopefully the optimiser won't remove the clearing code (best sensible effort)
            throw new SecurityException("key not cleared correctly");
        }
        key = null;
    }
}
