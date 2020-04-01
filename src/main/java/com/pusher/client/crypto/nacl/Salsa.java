/*
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

public class Salsa {
    public static final byte[] SIGMA = {'e', 'x', 'p', 'a', 'n', 'd', ' ', '3', '2', '-', 'b', 'y', 't', 'e', ' ', 'k'};

    private static final int ROUNDS = 20;

    private static long mask(byte x) {
        return 0xFFL & x;
    }

    // core applies the Salsa20 core function to 16-byte input in, 32-byte key k,
    // and 16-byte constant c, and puts the result into 64-byte array out.
    public static byte[] core(byte[] in, byte[] k, byte[] c) {
        byte[] out = new byte[64];
        long mask = 0xFFFFFFFFL;

        long j0 = mask & (mask(c[0]) | mask(c[1]) << 8 | mask(c[2]) << 16 | mask(c[3]) << 24);
        long j1 = mask & (mask(k[0]) | mask(k[1]) << 8 | mask(k[2]) << 16 | mask(k[3]) << 24);
        long j2 = mask & (mask(k[4]) | mask(k[5]) << 8 | mask(k[6]) << 16 | mask(k[7]) << 24);
        long j3 = mask & (mask(k[8]) | mask(k[9]) << 8 | mask(k[10]) << 16 | mask(k[11]) << 24);
        long j4 = mask & (mask(k[12]) | mask(k[13]) << 8 | mask(k[14]) << 16 | mask(k[15]) << 24);
        long j5 = mask & (mask(c[4]) | mask(c[5]) << 8 | mask(c[6]) << 16 | mask(c[7]) << 24);
        long j6 = mask & (mask(in[0]) | mask(in[1]) << 8 | mask(in[2]) << 16 | mask(in[3]) << 24);
        long j7 = mask & (mask(in[4]) | mask(in[5]) << 8 | mask(in[6]) << 16 | mask(in[7]) << 24);
        long j8 = mask & (mask(in[8]) | mask(in[9]) << 8 | mask(in[10]) << 16 | mask(in[11]) << 24);
        long j9 = mask & (mask(in[12]) | mask(in[13]) << 8 | mask(in[14]) << 16 | mask(in[15]) << 24);
        long j10 = mask & (mask(c[8]) | mask(c[9]) << 8 | mask(c[10]) << 16 | mask(c[11]) << 24);
        long j11 = mask & (mask(k[16]) | mask(k[17]) << 8 | mask(k[18]) << 16 | mask(k[19]) << 24);
        long j12 = mask & (mask(k[20]) | mask(k[21]) << 8 | mask(k[22]) << 16 | mask(k[23]) << 24);
        long j13 = mask & (mask(k[24]) | mask(k[25]) << 8 | mask(k[26]) << 16 | mask(k[27]) << 24);
        long j14 = mask & (mask(k[28]) | mask(k[29]) << 8 | mask(k[30]) << 16 | mask(k[31]) << 24);
        long j15 = mask & (mask(c[12]) | mask(c[13]) << 8 | mask(c[14]) << 16 | mask(c[15]) << 24);

        long x0 = j0, x1 = j1, x2 = j2, x3 = j3, x4 = j4;
        long x5 = j5, x6 = j6, x7 = j7, x8 = j8;
        long x9 = j9, x10 = j10, x11 = j11, x12 = j12;
        long x13 = j13, x14 = j14, x15 = j15;

        for (int i = 0; i < ROUNDS; i += 2) {
            long u = mask & (x0 + x12);
            x4 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x4 + x0);
            x8 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x8 + x4);
            x12 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x12 + x8);
            x0 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x5 + x1);
            x9 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x9 + x5);
            x13 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x13 + x9);
            x1 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x1 + x13);
            x5 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x10 + x6);
            x14 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x14 + x10);
            x2 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x2 + x14);
            x6 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x6 + x2);
            x10 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x15 + x11);
            x3 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x3 + x15);
            x7 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x7 + x3);
            x11 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x11 + x7);
            x15 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x0 + x3);
            x1 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x1 + x0);
            x2 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x2 + x1);
            x3 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x3 + x2);
            x0 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x5 + x4);
            x6 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x6 + x5);
            x7 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x7 + x6);
            x4 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x4 + x7);
            x5 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x10 + x9);
            x11 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x11 + x10);
            x8 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x8 + x11);
            x9 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x9 + x8);
            x10 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x15 + x14);
            x12 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x12 + x15);
            x13 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x13 + x12);
            x14 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x14 + x13);
            x15 ^= mask & (u << 18 | u >>> (32 - 18));
        }

        x0 += j0;
        x1 += j1;
        x2 += j2;
        x3 += j3;
        x4 += j4;
        x5 += j5;
        x6 += j6;
        x7 += j7;
        x8 += j8;
        x9 += j9;
        x10 += j10;
        x11 += j11;
        x12 += j12;
        x13 += j13;
        x14 += j14;
        x15 += j15;

        x0 &= mask;
        x1 &= mask;
        x2 &= mask;
        x3 &= mask;
        x4 &= mask;
        x5 &= mask;
        x6 &= mask;
        x7 &= mask;
        x8 &= mask;
        x9 &= mask;
        x10 &= mask;
        x11 &= mask;
        x12 &= mask;
        x13 &= mask;
        x14 &= mask;
        x15 &= mask;

        out[0] = (byte) (x0);
        out[1] = (byte) (x0 >> 8);
        out[2] = (byte) (x0 >> 16);
        out[3] = (byte) (x0 >> 24);

        out[4] = (byte) (x1);
        out[5] = (byte) (x1 >> 8);
        out[6] = (byte) (x1 >> 16);
        out[7] = (byte) (x1 >> 24);

        out[8] = (byte) (x2);
        out[9] = (byte) (x2 >> 8);
        out[10] = (byte) (x2 >> 16);
        out[11] = (byte) (x2 >> 24);

        out[12] = (byte) (x3);
        out[13] = (byte) (x3 >> 8);
        out[14] = (byte) (x3 >> 16);
        out[15] = (byte) (x3 >> 24);

        out[16] = (byte) (x4);
        out[17] = (byte) (x4 >> 8);
        out[18] = (byte) (x4 >> 16);
        out[19] = (byte) (x4 >> 24);

        out[20] = (byte) (x5);
        out[21] = (byte) (x5 >> 8);
        out[22] = (byte) (x5 >> 16);
        out[23] = (byte) (x5 >> 24);

        out[24] = (byte) (x6);
        out[25] = (byte) (x6 >> 8);
        out[26] = (byte) (x6 >> 16);
        out[27] = (byte) (x6 >> 24);

        out[28] = (byte) (x7);
        out[29] = (byte) (x7 >> 8);
        out[30] = (byte) (x7 >> 16);
        out[31] = (byte) (x7 >> 24);

        out[32] = (byte) (x8);
        out[33] = (byte) (x8 >> 8);
        out[34] = (byte) (x8 >> 16);
        out[35] = (byte) (x8 >> 24);

        out[36] = (byte) (x9);
        out[37] = (byte) (x9 >> 8);
        out[38] = (byte) (x9 >> 16);
        out[39] = (byte) (x9 >> 24);

        out[40] = (byte) (x10);
        out[41] = (byte) (x10 >> 8);
        out[42] = (byte) (x10 >> 16);
        out[43] = (byte) (x10 >> 24);

        out[44] = (byte) (x11);
        out[45] = (byte) (x11 >> 8);
        out[46] = (byte) (x11 >> 16);
        out[47] = (byte) (x11 >> 24);

        out[48] = (byte) (x12);
        out[49] = (byte) (x12 >> 8);
        out[50] = (byte) (x12 >> 16);
        out[51] = (byte) (x12 >> 24);

        out[52] = (byte) (x13);
        out[53] = (byte) (x13 >> 8);
        out[54] = (byte) (x13 >> 16);
        out[55] = (byte) (x13 >> 24);

        out[56] = (byte) (x14);
        out[57] = (byte) (x14 >> 8);
        out[58] = (byte) (x14 >> 16);
        out[59] = (byte) (x14 >> 24);

        out[60] = (byte) (x15);
        out[61] = (byte) (x15 >> 8);
        out[62] = (byte) (x15 >> 16);
        out[63] = (byte) (x15 >> 24);
        return out;
    }

    // XORKeyStream crypts bytes from in to out using the given key and counters.
    // In and out may be the same slice but otherwise should not overlap. Counter
    // contains the raw salsa20 counter bytes (both nonce and block counter).
    public static byte[] XORKeyStream(byte[] in, byte[] counter, byte[] key) {
        byte[] out = in.clone();
        byte[] block;
        byte[] counterCopy = counter.clone();

        int count = 0;
        while (in.length >= 64) {
            block = core(counterCopy, key, SIGMA);

            for (int i = 0; i < block.length; i++) {
                byte x = block[i];
                out[i + 64 * count] = (byte) (in[i] ^ x);
            }
            long u = 1;
            for (int i = 8; i < 16; i++) {
                u += 0xFF & counterCopy[i];
                counterCopy[i] = (byte) (u);
                u >>= 8;
            }
            byte[] temp = in.clone();
            in = new byte[in.length - 64];
            System.arraycopy(temp, 64, in, 0, in.length);

            count++;
        }

        if (in.length > 0) {
            block = core(counterCopy, key, SIGMA);

            for (int i = 0; i < in.length; i++) {
                out[i + count * 64] = (byte) (in[i] ^ block[i]);
            }
        }

        return out;
    }

    // HSalsa20 applies the HSalsa20 core function to a 16-byte input in, 32-byte
    // key k, and 16-byte constant c, and returns the result as the 32-byte array
    // out.
    public static byte[] HSalsa20(byte[] in, byte[] k, byte[] c) {
        long x0 = mask(c[0]) | mask(c[1]) << 8 | mask(c[2]) << 16 | mask(c[3]) << 24;
        long x1 = mask(k[0]) | mask(k[1]) << 8 | mask(k[2]) << 16 | mask(k[3]) << 24;
        long x2 = mask(k[4]) | mask(k[5]) << 8 | mask(k[6]) << 16 | mask(k[7]) << 24;
        long x3 = mask(k[8]) | mask(k[9]) << 8 | mask(k[10]) << 16 | mask(k[11]) << 24;
        long x4 = mask(k[12]) | mask(k[13]) << 8 | mask(k[14]) << 16 | mask(k[15]) << 24;
        long x5 = mask(c[4]) | mask(c[5]) << 8 | mask(c[6]) << 16 | mask(c[7]) << 24;
        long x6 = mask(in[0]) | mask(in[1]) << 8 | mask(in[2]) << 16 | mask(in[3]) << 24;
        long x7 = mask(in[4]) | mask(in[5]) << 8 | mask(in[6]) << 16 | mask(in[7]) << 24;
        long x8 = mask(in[8]) | mask(in[9]) << 8 | mask(in[10]) << 16 | mask(in[11]) << 24;
        long x9 = mask(in[12]) | mask(in[13]) << 8 | mask(in[14]) << 16 | mask(in[15]) << 24;
        long x10 = mask(c[8]) | mask(c[9]) << 8 | mask(c[10]) << 16 | mask(c[11]) << 24;
        long x11 = mask(k[16]) | mask(k[17]) << 8 | mask(k[18]) << 16 | mask(k[19]) << 24;
        long x12 = mask(k[20]) | mask(k[21]) << 8 | mask(k[22]) << 16 | mask(k[23]) << 24;
        long x13 = mask(k[24]) | mask(k[25]) << 8 | mask(k[26]) << 16 | mask(k[27]) << 24;
        long x14 = mask(k[28]) | mask(k[29]) << 8 | mask(k[30]) << 16 | mask(k[31]) << 24;
        long x15 = mask(c[12]) | mask(c[13]) << 8 | mask(c[14]) << 16 | mask(c[15]) << 24;

        long mask = 0xFFFFFFFFL;
        for (int i = 0; i < 20; i += 2) {
            long u = mask & (x0 + x12);
            x4 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x4 + x0);
            x8 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x8 + x4);
            x12 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x12 + x8);
            x0 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x5 + x1);
            x9 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x9 + x5);
            x13 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x13 + x9);
            x1 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x1 + x13);
            x5 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x10 + x6);
            x14 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x14 + x10);
            x2 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x2 + x14);
            x6 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x6 + x2);
            x10 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x15 + x11);
            x3 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x3 + x15);
            x7 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x7 + x3);
            x11 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x11 + x7);
            x15 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x0 + x3);
            x1 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x1 + x0);
            x2 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x2 + x1);
            x3 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x3 + x2);
            x0 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x5 + x4);
            x6 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x6 + x5);
            x7 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x7 + x6);
            x4 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x4 + x7);
            x5 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x10 + x9);
            x11 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x11 + x10);
            x8 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x8 + x11);
            x9 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x9 + x8);
            x10 ^= mask & (u << 18 | u >>> (32 - 18));

            u = mask & (x15 + x14);
            x12 ^= mask & (u << 7 | u >>> (32 - 7));
            u = mask & (x12 + x15);
            x13 ^= mask & (u << 9 | u >>> (32 - 9));
            u = mask & (x13 + x12);
            x14 ^= mask & (u << 13 | u >>> (32 - 13));
            u = mask & (x14 + x13);
            x15 ^= mask & (u << 18 | u >>> (32 - 18));
        }

        byte[] out = new byte[32];
        out[0] = (byte) x0;
        out[1] = (byte) (x0 >> 8);
        out[2] = (byte) (x0 >> 16);
        out[3] = (byte) (x0 >> 24);

        out[4] = (byte) (x5);
        out[5] = (byte) (x5 >> 8);
        out[6] = (byte) (x5 >> 16);
        out[7] = (byte) (x5 >> 24);

        out[8] = (byte) (x10);
        out[9] = (byte) (x10 >> 8);
        out[10] = (byte) (x10 >> 16);
        out[11] = (byte) (x10 >> 24);

        out[12] = (byte) (x15);
        out[13] = (byte) (x15 >> 8);
        out[14] = (byte) (x15 >> 16);
        out[15] = (byte) (x15 >> 24);

        out[16] = (byte) (x6);
        out[17] = (byte) (x6 >> 8);
        out[18] = (byte) (x6 >> 16);
        out[19] = (byte) (x6 >> 24);

        out[20] = (byte) (x7);
        out[21] = (byte) (x7 >> 8);
        out[22] = (byte) (x7 >> 16);
        out[23] = (byte) (x7 >> 24);

        out[24] = (byte) (x8);
        out[25] = (byte) (x8 >> 8);
        out[26] = (byte) (x8 >> 16);
        out[27] = (byte) (x8 >> 24);

        out[28] = (byte) (x9);
        out[29] = (byte) (x9 >> 8);
        out[30] = (byte) (x9 >> 16);
        out[31] = (byte) (x9 >> 24);
        return out;
    }
}
