package com.pusher.client.crypto.nacl;

// Copyright (c) 2014-present Tom Zhou<appnet.link@gmail.com>
//https://github.com/InstantWebP2P/tweetnacl-java

import java.util.concurrent.atomic.AtomicLong;

/*
 * @description
 *   TweetNacl.c Java porting
 * */
public final class TweetNaclFast {

    /*
     * @description
     *   Secret Box algorithm, secret key
     * */
    public static final class SecretBox {

        private final AtomicLong nonce;

        private final byte[] key;

        public SecretBox(byte[] key) {
            this(key, 68);
        }

        public SecretBox(byte[] key, long nonce) {
            this.key = key;

            this.nonce = new AtomicLong(nonce);
        }

        public byte[] open(byte[] box, byte[] theNonce) {
            if (box == null) return null;
            return open(box, 0, box.length, theNonce);
        }

        public byte[] open(byte[] box, final int boxoff, final int boxlen, byte[] theNonce) {
            // check message
            if (
                    !(
                            box != null &&
                                    box.length >= (boxoff + boxlen) &&
                                    boxlen >= boxzerobytesLength &&
                                    theNonce != null &&
                                    theNonce.length == nonceLength
                    )
            ) return null;

            // cipher buffer
            byte[] c = new byte[boxlen + boxzerobytesLength];

            // message buffer
            byte[] m = new byte[c.length];

            for (int i = 0; i < boxlen; i++) c[i + boxzerobytesLength] = box[i + boxoff];

            if (0 != crypto_secretbox_open(m, c, c.length, theNonce, key)) return null;

            // wrap byte_buf_t on m offset@zerobytesLength
            ///return new byte_buf_t(m, zerobytesLength, m.length-zerobytesLength);
            byte[] ret = new byte[m.length - zerobytesLength];

            for (int i = 0; i < ret.length; i++) ret[i] = m[i + zerobytesLength];

            return ret;
        }

        /*
         * @description
         *   Length of nonce in bytes.
         * */
        public static final int nonceLength = 24;

        /*
         * @description
         *   zero bytes in case box
         * */
        public static final int zerobytesLength = 32;
        /*
         * @description
         *   zero bytes in case open box
         * */
        public static final int boxzerobytesLength = 16;
    }

    private static int vn(byte[] x, final int xoff, byte[] y, final int yoff, int n) {
        int i, d = 0;
        for (i = 0; i < n; i++) d |= (x[i + xoff] ^ y[i + yoff]) & 0xff;
        return (1 & ((d - 1) >>> 8)) - 1;
    }

    private static int crypto_verify_16(byte[] x, final int xoff, byte[] y, final int yoff) {
        return vn(x, xoff, y, yoff, 16);
    }

    private static void core_salsa20(byte[] o, byte[] p, byte[] k, byte[] c) {
        int j0 = c[0] & 0xff | (c[1] & 0xff) << 8 | (c[2] & 0xff) << 16 | (c[3] & 0xff) << 24, j1 =
                k[0] & 0xff | (k[1] & 0xff) << 8 | (k[2] & 0xff) << 16 | (k[3] & 0xff) << 24, j2 =
                k[4] & 0xff | (k[5] & 0xff) << 8 | (k[6] & 0xff) << 16 | (k[7] & 0xff) << 24, j3 =
                k[8] & 0xff | (k[9] & 0xff) << 8 | (k[10] & 0xff) << 16 | (k[11] & 0xff) << 24, j4 =
                k[12] & 0xff | (k[13] & 0xff) << 8 | (k[14] & 0xff) << 16 | (k[15] & 0xff) << 24, j5 =
                c[4] & 0xff | (c[5] & 0xff) << 8 | (c[6] & 0xff) << 16 | (c[7] & 0xff) << 24, j6 =
                p[0] & 0xff | (p[1] & 0xff) << 8 | (p[2] & 0xff) << 16 | (p[3] & 0xff) << 24, j7 =
                p[4] & 0xff | (p[5] & 0xff) << 8 | (p[6] & 0xff) << 16 | (p[7] & 0xff) << 24, j8 =
                p[8] & 0xff | (p[9] & 0xff) << 8 | (p[10] & 0xff) << 16 | (p[11] & 0xff) << 24, j9 =
                p[12] & 0xff | (p[13] & 0xff) << 8 | (p[14] & 0xff) << 16 | (p[15] & 0xff) << 24, j10 =
                c[8] & 0xff | (c[9] & 0xff) << 8 | (c[10] & 0xff) << 16 | (c[11] & 0xff) << 24, j11 =
                k[16] & 0xff | (k[17] & 0xff) << 8 | (k[18] & 0xff) << 16 | (k[19] & 0xff) << 24, j12 =
                k[20] & 0xff | (k[21] & 0xff) << 8 | (k[22] & 0xff) << 16 | (k[23] & 0xff) << 24, j13 =
                k[24] & 0xff | (k[25] & 0xff) << 8 | (k[26] & 0xff) << 16 | (k[27] & 0xff) << 24, j14 =
                k[28] & 0xff | (k[29] & 0xff) << 8 | (k[30] & 0xff) << 16 | (k[31] & 0xff) << 24, j15 =
                c[12] & 0xff | (c[13] & 0xff) << 8 | (c[14] & 0xff) << 16 | (c[15] & 0xff) << 24;

        int x0 = j0, x1 = j1, x2 = j2, x3 = j3, x4 = j4, x5 = j5, x6 = j6, x7 = j7, x8 = j8, x9 = j9, x10 = j10, x11 =
                j11, x12 = j12, x13 = j13, x14 = j14, x15 = j15, u;

        for (int i = 0; i < 20; i += 2) {
            u = x0 + x12 | 0;
            x4 ^= u << 7 | u >>> (32 - 7);
            u = x4 + x0 | 0;
            x8 ^= u << 9 | u >>> (32 - 9);
            u = x8 + x4 | 0;
            x12 ^= u << 13 | u >>> (32 - 13);
            u = x12 + x8 | 0;
            x0 ^= u << 18 | u >>> (32 - 18);

            u = x5 + x1 | 0;
            x9 ^= u << 7 | u >>> (32 - 7);
            u = x9 + x5 | 0;
            x13 ^= u << 9 | u >>> (32 - 9);
            u = x13 + x9 | 0;
            x1 ^= u << 13 | u >>> (32 - 13);
            u = x1 + x13 | 0;
            x5 ^= u << 18 | u >>> (32 - 18);

            u = x10 + x6 | 0;
            x14 ^= u << 7 | u >>> (32 - 7);
            u = x14 + x10 | 0;
            x2 ^= u << 9 | u >>> (32 - 9);
            u = x2 + x14 | 0;
            x6 ^= u << 13 | u >>> (32 - 13);
            u = x6 + x2 | 0;
            x10 ^= u << 18 | u >>> (32 - 18);

            u = x15 + x11 | 0;
            x3 ^= u << 7 | u >>> (32 - 7);
            u = x3 + x15 | 0;
            x7 ^= u << 9 | u >>> (32 - 9);
            u = x7 + x3 | 0;
            x11 ^= u << 13 | u >>> (32 - 13);
            u = x11 + x7 | 0;
            x15 ^= u << 18 | u >>> (32 - 18);

            u = x0 + x3 | 0;
            x1 ^= u << 7 | u >>> (32 - 7);
            u = x1 + x0 | 0;
            x2 ^= u << 9 | u >>> (32 - 9);
            u = x2 + x1 | 0;
            x3 ^= u << 13 | u >>> (32 - 13);
            u = x3 + x2 | 0;
            x0 ^= u << 18 | u >>> (32 - 18);

            u = x5 + x4 | 0;
            x6 ^= u << 7 | u >>> (32 - 7);
            u = x6 + x5 | 0;
            x7 ^= u << 9 | u >>> (32 - 9);
            u = x7 + x6 | 0;
            x4 ^= u << 13 | u >>> (32 - 13);
            u = x4 + x7 | 0;
            x5 ^= u << 18 | u >>> (32 - 18);

            u = x10 + x9 | 0;
            x11 ^= u << 7 | u >>> (32 - 7);
            u = x11 + x10 | 0;
            x8 ^= u << 9 | u >>> (32 - 9);
            u = x8 + x11 | 0;
            x9 ^= u << 13 | u >>> (32 - 13);
            u = x9 + x8 | 0;
            x10 ^= u << 18 | u >>> (32 - 18);

            u = x15 + x14 | 0;
            x12 ^= u << 7 | u >>> (32 - 7);
            u = x12 + x15 | 0;
            x13 ^= u << 9 | u >>> (32 - 9);
            u = x13 + x12 | 0;
            x14 ^= u << 13 | u >>> (32 - 13);
            u = x14 + x13 | 0;
            x15 ^= u << 18 | u >>> (32 - 18);
        }
        x0 = x0 + j0 | 0;
        x1 = x1 + j1 | 0;
        x2 = x2 + j2 | 0;
        x3 = x3 + j3 | 0;
        x4 = x4 + j4 | 0;
        x5 = x5 + j5 | 0;
        x6 = x6 + j6 | 0;
        x7 = x7 + j7 | 0;
        x8 = x8 + j8 | 0;
        x9 = x9 + j9 | 0;
        x10 = x10 + j10 | 0;
        x11 = x11 + j11 | 0;
        x12 = x12 + j12 | 0;
        x13 = x13 + j13 | 0;
        x14 = x14 + j14 | 0;
        x15 = x15 + j15 | 0;

        o[0] = (byte) (x0 >>> 0 & 0xff);
        o[1] = (byte) (x0 >>> 8 & 0xff);
        o[2] = (byte) (x0 >>> 16 & 0xff);
        o[3] = (byte) (x0 >>> 24 & 0xff);

        o[4] = (byte) (x1 >>> 0 & 0xff);
        o[5] = (byte) (x1 >>> 8 & 0xff);
        o[6] = (byte) (x1 >>> 16 & 0xff);
        o[7] = (byte) (x1 >>> 24 & 0xff);

        o[8] = (byte) (x2 >>> 0 & 0xff);
        o[9] = (byte) (x2 >>> 8 & 0xff);
        o[10] = (byte) (x2 >>> 16 & 0xff);
        o[11] = (byte) (x2 >>> 24 & 0xff);

        o[12] = (byte) (x3 >>> 0 & 0xff);
        o[13] = (byte) (x3 >>> 8 & 0xff);
        o[14] = (byte) (x3 >>> 16 & 0xff);
        o[15] = (byte) (x3 >>> 24 & 0xff);

        o[16] = (byte) (x4 >>> 0 & 0xff);
        o[17] = (byte) (x4 >>> 8 & 0xff);
        o[18] = (byte) (x4 >>> 16 & 0xff);
        o[19] = (byte) (x4 >>> 24 & 0xff);

        o[20] = (byte) (x5 >>> 0 & 0xff);
        o[21] = (byte) (x5 >>> 8 & 0xff);
        o[22] = (byte) (x5 >>> 16 & 0xff);
        o[23] = (byte) (x5 >>> 24 & 0xff);

        o[24] = (byte) (x6 >>> 0 & 0xff);
        o[25] = (byte) (x6 >>> 8 & 0xff);
        o[26] = (byte) (x6 >>> 16 & 0xff);
        o[27] = (byte) (x6 >>> 24 & 0xff);

        o[28] = (byte) (x7 >>> 0 & 0xff);
        o[29] = (byte) (x7 >>> 8 & 0xff);
        o[30] = (byte) (x7 >>> 16 & 0xff);
        o[31] = (byte) (x7 >>> 24 & 0xff);

        o[32] = (byte) (x8 >>> 0 & 0xff);
        o[33] = (byte) (x8 >>> 8 & 0xff);
        o[34] = (byte) (x8 >>> 16 & 0xff);
        o[35] = (byte) (x8 >>> 24 & 0xff);

        o[36] = (byte) (x9 >>> 0 & 0xff);
        o[37] = (byte) (x9 >>> 8 & 0xff);
        o[38] = (byte) (x9 >>> 16 & 0xff);
        o[39] = (byte) (x9 >>> 24 & 0xff);

        o[40] = (byte) (x10 >>> 0 & 0xff);
        o[41] = (byte) (x10 >>> 8 & 0xff);
        o[42] = (byte) (x10 >>> 16 & 0xff);
        o[43] = (byte) (x10 >>> 24 & 0xff);

        o[44] = (byte) (x11 >>> 0 & 0xff);
        o[45] = (byte) (x11 >>> 8 & 0xff);
        o[46] = (byte) (x11 >>> 16 & 0xff);
        o[47] = (byte) (x11 >>> 24 & 0xff);

        o[48] = (byte) (x12 >>> 0 & 0xff);
        o[49] = (byte) (x12 >>> 8 & 0xff);
        o[50] = (byte) (x12 >>> 16 & 0xff);
        o[51] = (byte) (x12 >>> 24 & 0xff);

        o[52] = (byte) (x13 >>> 0 & 0xff);
        o[53] = (byte) (x13 >>> 8 & 0xff);
        o[54] = (byte) (x13 >>> 16 & 0xff);
        o[55] = (byte) (x13 >>> 24 & 0xff);

        o[56] = (byte) (x14 >>> 0 & 0xff);
        o[57] = (byte) (x14 >>> 8 & 0xff);
        o[58] = (byte) (x14 >>> 16 & 0xff);
        o[59] = (byte) (x14 >>> 24 & 0xff);

        o[60] = (byte) (x15 >>> 0 & 0xff);
        o[61] = (byte) (x15 >>> 8 & 0xff);
        o[62] = (byte) (x15 >>> 16 & 0xff);
        o[63] = (byte) (x15 >>> 24 & 0xff);
        /*String dbgt = "";
		for (int dbg = 0; dbg < o.length; dbg ++) dbgt += " "+o[dbg];
		Log.d(TAG, "core_salsa20 -> "+dbgt);
*/
    }

    private static void core_hsalsa20(byte[] o, byte[] p, byte[] k, byte[] c) {
        int j0 = c[0] & 0xff | (c[1] & 0xff) << 8 | (c[2] & 0xff) << 16 | (c[3] & 0xff) << 24, j1 =
                k[0] & 0xff | (k[1] & 0xff) << 8 | (k[2] & 0xff) << 16 | (k[3] & 0xff) << 24, j2 =
                k[4] & 0xff | (k[5] & 0xff) << 8 | (k[6] & 0xff) << 16 | (k[7] & 0xff) << 24, j3 =
                k[8] & 0xff | (k[9] & 0xff) << 8 | (k[10] & 0xff) << 16 | (k[11] & 0xff) << 24, j4 =
                k[12] & 0xff | (k[13] & 0xff) << 8 | (k[14] & 0xff) << 16 | (k[15] & 0xff) << 24, j5 =
                c[4] & 0xff | (c[5] & 0xff) << 8 | (c[6] & 0xff) << 16 | (c[7] & 0xff) << 24, j6 =
                p[0] & 0xff | (p[1] & 0xff) << 8 | (p[2] & 0xff) << 16 | (p[3] & 0xff) << 24, j7 =
                p[4] & 0xff | (p[5] & 0xff) << 8 | (p[6] & 0xff) << 16 | (p[7] & 0xff) << 24, j8 =
                p[8] & 0xff | (p[9] & 0xff) << 8 | (p[10] & 0xff) << 16 | (p[11] & 0xff) << 24, j9 =
                p[12] & 0xff | (p[13] & 0xff) << 8 | (p[14] & 0xff) << 16 | (p[15] & 0xff) << 24, j10 =
                c[8] & 0xff | (c[9] & 0xff) << 8 | (c[10] & 0xff) << 16 | (c[11] & 0xff) << 24, j11 =
                k[16] & 0xff | (k[17] & 0xff) << 8 | (k[18] & 0xff) << 16 | (k[19] & 0xff) << 24, j12 =
                k[20] & 0xff | (k[21] & 0xff) << 8 | (k[22] & 0xff) << 16 | (k[23] & 0xff) << 24, j13 =
                k[24] & 0xff | (k[25] & 0xff) << 8 | (k[26] & 0xff) << 16 | (k[27] & 0xff) << 24, j14 =
                k[28] & 0xff | (k[29] & 0xff) << 8 | (k[30] & 0xff) << 16 | (k[31] & 0xff) << 24, j15 =
                c[12] & 0xff | (c[13] & 0xff) << 8 | (c[14] & 0xff) << 16 | (c[15] & 0xff) << 24;

        int x0 = j0, x1 = j1, x2 = j2, x3 = j3, x4 = j4, x5 = j5, x6 = j6, x7 = j7, x8 = j8, x9 = j9, x10 = j10, x11 =
                j11, x12 = j12, x13 = j13, x14 = j14, x15 = j15, u;

        for (int i = 0; i < 20; i += 2) {
            u = x0 + x12 | 0;
            x4 ^= u << 7 | u >>> (32 - 7);
            u = x4 + x0 | 0;
            x8 ^= u << 9 | u >>> (32 - 9);
            u = x8 + x4 | 0;
            x12 ^= u << 13 | u >>> (32 - 13);
            u = x12 + x8 | 0;
            x0 ^= u << 18 | u >>> (32 - 18);

            u = x5 + x1 | 0;
            x9 ^= u << 7 | u >>> (32 - 7);
            u = x9 + x5 | 0;
            x13 ^= u << 9 | u >>> (32 - 9);
            u = x13 + x9 | 0;
            x1 ^= u << 13 | u >>> (32 - 13);
            u = x1 + x13 | 0;
            x5 ^= u << 18 | u >>> (32 - 18);

            u = x10 + x6 | 0;
            x14 ^= u << 7 | u >>> (32 - 7);
            u = x14 + x10 | 0;
            x2 ^= u << 9 | u >>> (32 - 9);
            u = x2 + x14 | 0;
            x6 ^= u << 13 | u >>> (32 - 13);
            u = x6 + x2 | 0;
            x10 ^= u << 18 | u >>> (32 - 18);

            u = x15 + x11 | 0;
            x3 ^= u << 7 | u >>> (32 - 7);
            u = x3 + x15 | 0;
            x7 ^= u << 9 | u >>> (32 - 9);
            u = x7 + x3 | 0;
            x11 ^= u << 13 | u >>> (32 - 13);
            u = x11 + x7 | 0;
            x15 ^= u << 18 | u >>> (32 - 18);

            u = x0 + x3 | 0;
            x1 ^= u << 7 | u >>> (32 - 7);
            u = x1 + x0 | 0;
            x2 ^= u << 9 | u >>> (32 - 9);
            u = x2 + x1 | 0;
            x3 ^= u << 13 | u >>> (32 - 13);
            u = x3 + x2 | 0;
            x0 ^= u << 18 | u >>> (32 - 18);

            u = x5 + x4 | 0;
            x6 ^= u << 7 | u >>> (32 - 7);
            u = x6 + x5 | 0;
            x7 ^= u << 9 | u >>> (32 - 9);
            u = x7 + x6 | 0;
            x4 ^= u << 13 | u >>> (32 - 13);
            u = x4 + x7 | 0;
            x5 ^= u << 18 | u >>> (32 - 18);

            u = x10 + x9 | 0;
            x11 ^= u << 7 | u >>> (32 - 7);
            u = x11 + x10 | 0;
            x8 ^= u << 9 | u >>> (32 - 9);
            u = x8 + x11 | 0;
            x9 ^= u << 13 | u >>> (32 - 13);
            u = x9 + x8 | 0;
            x10 ^= u << 18 | u >>> (32 - 18);

            u = x15 + x14 | 0;
            x12 ^= u << 7 | u >>> (32 - 7);
            u = x12 + x15 | 0;
            x13 ^= u << 9 | u >>> (32 - 9);
            u = x13 + x12 | 0;
            x14 ^= u << 13 | u >>> (32 - 13);
            u = x14 + x13 | 0;
            x15 ^= u << 18 | u >>> (32 - 18);
        }

        o[0] = (byte) (x0 >>> 0 & 0xff);
        o[1] = (byte) (x0 >>> 8 & 0xff);
        o[2] = (byte) (x0 >>> 16 & 0xff);
        o[3] = (byte) (x0 >>> 24 & 0xff);

        o[4] = (byte) (x5 >>> 0 & 0xff);
        o[5] = (byte) (x5 >>> 8 & 0xff);
        o[6] = (byte) (x5 >>> 16 & 0xff);
        o[7] = (byte) (x5 >>> 24 & 0xff);

        o[8] = (byte) (x10 >>> 0 & 0xff);
        o[9] = (byte) (x10 >>> 8 & 0xff);
        o[10] = (byte) (x10 >>> 16 & 0xff);
        o[11] = (byte) (x10 >>> 24 & 0xff);

        o[12] = (byte) (x15 >>> 0 & 0xff);
        o[13] = (byte) (x15 >>> 8 & 0xff);
        o[14] = (byte) (x15 >>> 16 & 0xff);
        o[15] = (byte) (x15 >>> 24 & 0xff);

        o[16] = (byte) (x6 >>> 0 & 0xff);
        o[17] = (byte) (x6 >>> 8 & 0xff);
        o[18] = (byte) (x6 >>> 16 & 0xff);
        o[19] = (byte) (x6 >>> 24 & 0xff);

        o[20] = (byte) (x7 >>> 0 & 0xff);
        o[21] = (byte) (x7 >>> 8 & 0xff);
        o[22] = (byte) (x7 >>> 16 & 0xff);
        o[23] = (byte) (x7 >>> 24 & 0xff);

        o[24] = (byte) (x8 >>> 0 & 0xff);
        o[25] = (byte) (x8 >>> 8 & 0xff);
        o[26] = (byte) (x8 >>> 16 & 0xff);
        o[27] = (byte) (x8 >>> 24 & 0xff);

        o[28] = (byte) (x9 >>> 0 & 0xff);
        o[29] = (byte) (x9 >>> 8 & 0xff);
        o[30] = (byte) (x9 >>> 16 & 0xff);
        o[31] = (byte) (x9 >>> 24 & 0xff);
        /*String dbgt = "";
		for (int dbg = 0; dbg < o.length; dbg ++) dbgt += " "+o[dbg];
		Log.d(TAG, "core_hsalsa20 -> "+dbgt);
*/
    }

    public static int crypto_core_salsa20(byte[] out, byte[] in, byte[] k, byte[] c) {
        ///core(out,in,k,c,0);
        core_salsa20(out, in, k, c);

        ///String dbgt = "";
        ///for (int dbg = 0; dbg < out.length; dbg ++) dbgt += " "+out[dbg];
        ///L/og.d(TAG, "crypto_core_salsa20 -> "+dbgt);

        return 0;
    }

    public static int crypto_core_hsalsa20(byte[] out, byte[] in, byte[] k, byte[] c) {
        ///core(out,in,k,c,1);
        core_hsalsa20(out, in, k, c);

        ///String dbgt = "";
        ///for (int dbg = 0; dbg < out.length; dbg ++) dbgt += " "+out[dbg];
        ///L/og.d(TAG, "crypto_core_hsalsa20 -> "+dbgt);

        return 0;
    }

    // "expand 32-byte k"
    private static final byte[] sigma = {101, 120, 112, 97, 110, 100, 32, 51, 50, 45, 98, 121, 116, 101, 32, 107};

    /*static {
		try {
			sigma = "expand 32-byte k".getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}*/

    private static int crypto_stream_salsa20_xor(byte[] c, int cpos, byte[] m, int mpos, long b, byte[] n, byte[] k) {
        byte[] z = new byte[16], x = new byte[64];
        int u, i;
        for (i = 0; i < 16; i++) z[i] = 0;
        for (i = 0; i < 8; i++) z[i] = n[i];
        while (b >= 64) {
            crypto_core_salsa20(x, z, k, sigma);
            for (i = 0; i < 64; i++) c[cpos + i] = (byte) ((m[mpos + i] ^ x[i]) & 0xff);
            u = 1;
            for (i = 8; i < 16; i++) {
                u = u + (z[i] & 0xff) | 0;
                z[i] = (byte) (u & 0xff);
                u >>>= 8;
            }
            b -= 64;
            cpos += 64;
            mpos += 64;
        }
        if (b > 0) {
            crypto_core_salsa20(x, z, k, sigma);
            for (i = 0; i < b; i++) c[cpos + i] = (byte) ((m[mpos + i] ^ x[i]) & 0xff);
        }

        ///String dbgt = "";
        ///for (int dbg = 0; dbg < c.length-cpos; dbg ++) dbgt += " "+c[dbg +cpos];
        ///Log.d(TAG, "crypto_stream_salsa20_xor, c -> "+dbgt);

        return 0;
    }

    public static int crypto_stream_salsa20(byte[] c, int cpos, long b, byte[] n, byte[] k) {
        byte[] z = new byte[16], x = new byte[64];
        int u, i;
        for (i = 0; i < 16; i++) z[i] = 0;
        for (i = 0; i < 8; i++) z[i] = n[i];
        while (b >= 64) {
            crypto_core_salsa20(x, z, k, sigma);
            for (i = 0; i < 64; i++) c[cpos + i] = x[i];
            u = 1;
            for (i = 8; i < 16; i++) {
                u = u + (z[i] & 0xff) | 0;
                z[i] = (byte) (u & 0xff);
                u >>>= 8;
            }
            b -= 64;
            cpos += 64;
        }
        if (b > 0) {
            crypto_core_salsa20(x, z, k, sigma);
            for (i = 0; i < b; i++) c[cpos + i] = x[i];
        }

        ///String dbgt = "";
        ///for (int dbg = 0; dbg < c.length-cpos; dbg ++) dbgt += " "+c[dbg +cpos];
        ///Log.d(TAG, "crypto_stream_salsa20, c -> "+dbgt);

        return 0;
    }

    public static int crypto_stream(byte[] c, int cpos, long d, byte[] n, byte[] k) {
        byte[] s = new byte[32];
        crypto_core_hsalsa20(s, n, k, sigma);
        byte[] sn = new byte[8];
        for (int i = 0; i < 8; i++) sn[i] = n[i + 16];
        return crypto_stream_salsa20(c, cpos, d, sn, s);
    }

    public static int crypto_stream_xor(byte[] c, int cpos, byte[] m, int mpos, long d, byte[] n, byte[] k) {
        byte[] s = new byte[32];

        /*String dbgt = "";
		for (int dbg = 0; dbg < n.length; dbg ++) dbgt += " "+n[dbg];
		Log.d(TAG, "crypto_stream_xor, nonce -> "+dbgt);

		dbgt = "";
		for (int dbg = 0; dbg < k.length; dbg ++) dbgt += " "+k[dbg];
		Log.d(TAG, "crypto_stream_xor, shk -> "+dbgt);
		*/

        crypto_core_hsalsa20(s, n, k, sigma);
        byte[] sn = new byte[8];
        for (int i = 0; i < 8; i++) sn[i] = n[i + 16];
        return crypto_stream_salsa20_xor(c, cpos, m, mpos, d, sn, s);
    }

    /*
     * Port of Andrew Moon's Poly1305-donna-16. Public domain.
     * https://github.com/floodyberry/poly1305-donna
     */
    public static final class poly1305 {

        private final byte[] buffer;
        private final int[] r;
        private final int[] h;
        private final int[] pad;
        private int leftover;
        private int fin;

        public poly1305(byte[] key) {
            this.buffer = new byte[16];
            this.r = new int[10];
            this.h = new int[10];
            this.pad = new int[8];
            this.leftover = 0;
            this.fin = 0;

            int t0, t1, t2, t3, t4, t5, t6, t7;

            t0 = key[0] & 0xff | (key[1] & 0xff) << 8;
            this.r[0] = (t0) & 0x1fff;
            t1 = key[2] & 0xff | (key[3] & 0xff) << 8;
            this.r[1] = ((t0 >>> 13) | (t1 << 3)) & 0x1fff;
            t2 = key[4] & 0xff | (key[5] & 0xff) << 8;
            this.r[2] = ((t1 >>> 10) | (t2 << 6)) & 0x1f03;
            t3 = key[6] & 0xff | (key[7] & 0xff) << 8;
            this.r[3] = ((t2 >>> 7) | (t3 << 9)) & 0x1fff;
            t4 = key[8] & 0xff | (key[9] & 0xff) << 8;
            this.r[4] = ((t3 >>> 4) | (t4 << 12)) & 0x00ff;
            this.r[5] = ((t4 >>> 1)) & 0x1ffe;
            t5 = key[10] & 0xff | (key[11] & 0xff) << 8;
            this.r[6] = ((t4 >>> 14) | (t5 << 2)) & 0x1fff;
            t6 = key[12] & 0xff | (key[13] & 0xff) << 8;
            this.r[7] = ((t5 >>> 11) | (t6 << 5)) & 0x1f81;
            t7 = key[14] & 0xff | (key[15] & 0xff) << 8;
            this.r[8] = ((t6 >>> 8) | (t7 << 8)) & 0x1fff;
            this.r[9] = ((t7 >>> 5)) & 0x007f;

            this.pad[0] = key[16] & 0xff | (key[17] & 0xff) << 8;
            this.pad[1] = key[18] & 0xff | (key[19] & 0xff) << 8;
            this.pad[2] = key[20] & 0xff | (key[21] & 0xff) << 8;
            this.pad[3] = key[22] & 0xff | (key[23] & 0xff) << 8;
            this.pad[4] = key[24] & 0xff | (key[25] & 0xff) << 8;
            this.pad[5] = key[26] & 0xff | (key[27] & 0xff) << 8;
            this.pad[6] = key[28] & 0xff | (key[29] & 0xff) << 8;
            this.pad[7] = key[30] & 0xff | (key[31] & 0xff) << 8;
        }

        public poly1305 blocks(byte[] m, int mpos, int bytes) {
            int hibit = this.fin != 0 ? 0 : (1 << 11);
            int t0, t1, t2, t3, t4, t5, t6, t7, c;
            int d0, d1, d2, d3, d4, d5, d6, d7, d8, d9;

            int h0 = this.h[0], h1 = this.h[1], h2 = this.h[2], h3 = this.h[3], h4 = this.h[4], h5 = this.h[5], h6 =
                    this.h[6], h7 = this.h[7], h8 = this.h[8], h9 = this.h[9];

            int r0 = this.r[0], r1 = this.r[1], r2 = this.r[2], r3 = this.r[3], r4 = this.r[4], r5 = this.r[5], r6 =
                    this.r[6], r7 = this.r[7], r8 = this.r[8], r9 = this.r[9];

            while (bytes >= 16) {
                t0 = m[mpos + 0] & 0xff | (m[mpos + 1] & 0xff) << 8;
                h0 += (t0) & 0x1fff;
                t1 = m[mpos + 2] & 0xff | (m[mpos + 3] & 0xff) << 8;
                h1 += ((t0 >>> 13) | (t1 << 3)) & 0x1fff;
                t2 = m[mpos + 4] & 0xff | (m[mpos + 5] & 0xff) << 8;
                h2 += ((t1 >>> 10) | (t2 << 6)) & 0x1fff;
                t3 = m[mpos + 6] & 0xff | (m[mpos + 7] & 0xff) << 8;
                h3 += ((t2 >>> 7) | (t3 << 9)) & 0x1fff;
                t4 = m[mpos + 8] & 0xff | (m[mpos + 9] & 0xff) << 8;
                h4 += ((t3 >>> 4) | (t4 << 12)) & 0x1fff;
                h5 += ((t4 >>> 1)) & 0x1fff;
                t5 = m[mpos + 10] & 0xff | (m[mpos + 11] & 0xff) << 8;
                h6 += ((t4 >>> 14) | (t5 << 2)) & 0x1fff;
                t6 = m[mpos + 12] & 0xff | (m[mpos + 13] & 0xff) << 8;
                h7 += ((t5 >>> 11) | (t6 << 5)) & 0x1fff;
                t7 = m[mpos + 14] & 0xff | (m[mpos + 15] & 0xff) << 8;
                h8 += ((t6 >>> 8) | (t7 << 8)) & 0x1fff;
                h9 += ((t7 >>> 5)) | hibit;

                c = 0;

                d0 = c;
                d0 += h0 * r0;
                d0 += h1 * (5 * r9);
                d0 += h2 * (5 * r8);
                d0 += h3 * (5 * r7);
                d0 += h4 * (5 * r6);
                c = (d0 >>> 13);
                d0 &= 0x1fff;
                d0 += h5 * (5 * r5);
                d0 += h6 * (5 * r4);
                d0 += h7 * (5 * r3);
                d0 += h8 * (5 * r2);
                d0 += h9 * (5 * r1);
                c += (d0 >>> 13);
                d0 &= 0x1fff;

                d1 = c;
                d1 += h0 * r1;
                d1 += h1 * r0;
                d1 += h2 * (5 * r9);
                d1 += h3 * (5 * r8);
                d1 += h4 * (5 * r7);
                c = (d1 >>> 13);
                d1 &= 0x1fff;
                d1 += h5 * (5 * r6);
                d1 += h6 * (5 * r5);
                d1 += h7 * (5 * r4);
                d1 += h8 * (5 * r3);
                d1 += h9 * (5 * r2);
                c += (d1 >>> 13);
                d1 &= 0x1fff;

                d2 = c;
                d2 += h0 * r2;
                d2 += h1 * r1;
                d2 += h2 * r0;
                d2 += h3 * (5 * r9);
                d2 += h4 * (5 * r8);
                c = (d2 >>> 13);
                d2 &= 0x1fff;
                d2 += h5 * (5 * r7);
                d2 += h6 * (5 * r6);
                d2 += h7 * (5 * r5);
                d2 += h8 * (5 * r4);
                d2 += h9 * (5 * r3);
                c += (d2 >>> 13);
                d2 &= 0x1fff;

                d3 = c;
                d3 += h0 * r3;
                d3 += h1 * r2;
                d3 += h2 * r1;
                d3 += h3 * r0;
                d3 += h4 * (5 * r9);
                c = (d3 >>> 13);
                d3 &= 0x1fff;
                d3 += h5 * (5 * r8);
                d3 += h6 * (5 * r7);
                d3 += h7 * (5 * r6);
                d3 += h8 * (5 * r5);
                d3 += h9 * (5 * r4);
                c += (d3 >>> 13);
                d3 &= 0x1fff;

                d4 = c;
                d4 += h0 * r4;
                d4 += h1 * r3;
                d4 += h2 * r2;
                d4 += h3 * r1;
                d4 += h4 * r0;
                c = (d4 >>> 13);
                d4 &= 0x1fff;
                d4 += h5 * (5 * r9);
                d4 += h6 * (5 * r8);
                d4 += h7 * (5 * r7);
                d4 += h8 * (5 * r6);
                d4 += h9 * (5 * r5);
                c += (d4 >>> 13);
                d4 &= 0x1fff;

                d5 = c;
                d5 += h0 * r5;
                d5 += h1 * r4;
                d5 += h2 * r3;
                d5 += h3 * r2;
                d5 += h4 * r1;
                c = (d5 >>> 13);
                d5 &= 0x1fff;
                d5 += h5 * r0;
                d5 += h6 * (5 * r9);
                d5 += h7 * (5 * r8);
                d5 += h8 * (5 * r7);
                d5 += h9 * (5 * r6);
                c += (d5 >>> 13);
                d5 &= 0x1fff;

                d6 = c;
                d6 += h0 * r6;
                d6 += h1 * r5;
                d6 += h2 * r4;
                d6 += h3 * r3;
                d6 += h4 * r2;
                c = (d6 >>> 13);
                d6 &= 0x1fff;
                d6 += h5 * r1;
                d6 += h6 * r0;
                d6 += h7 * (5 * r9);
                d6 += h8 * (5 * r8);
                d6 += h9 * (5 * r7);
                c += (d6 >>> 13);
                d6 &= 0x1fff;

                d7 = c;
                d7 += h0 * r7;
                d7 += h1 * r6;
                d7 += h2 * r5;
                d7 += h3 * r4;
                d7 += h4 * r3;
                c = (d7 >>> 13);
                d7 &= 0x1fff;
                d7 += h5 * r2;
                d7 += h6 * r1;
                d7 += h7 * r0;
                d7 += h8 * (5 * r9);
                d7 += h9 * (5 * r8);
                c += (d7 >>> 13);
                d7 &= 0x1fff;

                d8 = c;
                d8 += h0 * r8;
                d8 += h1 * r7;
                d8 += h2 * r6;
                d8 += h3 * r5;
                d8 += h4 * r4;
                c = (d8 >>> 13);
                d8 &= 0x1fff;
                d8 += h5 * r3;
                d8 += h6 * r2;
                d8 += h7 * r1;
                d8 += h8 * r0;
                d8 += h9 * (5 * r9);
                c += (d8 >>> 13);
                d8 &= 0x1fff;

                d9 = c;
                d9 += h0 * r9;
                d9 += h1 * r8;
                d9 += h2 * r7;
                d9 += h3 * r6;
                d9 += h4 * r5;
                c = (d9 >>> 13);
                d9 &= 0x1fff;
                d9 += h5 * r4;
                d9 += h6 * r3;
                d9 += h7 * r2;
                d9 += h8 * r1;
                d9 += h9 * r0;
                c += (d9 >>> 13);
                d9 &= 0x1fff;

                c = (((c << 2) + c)) | 0;
                c = (c + d0) | 0;
                d0 = c & 0x1fff;
                c = (c >>> 13);
                d1 += c;

                h0 = d0;
                h1 = d1;
                h2 = d2;
                h3 = d3;
                h4 = d4;
                h5 = d5;
                h6 = d6;
                h7 = d7;
                h8 = d8;
                h9 = d9;

                mpos += 16;
                bytes -= 16;
            }
            this.h[0] = h0;
            this.h[1] = h1;
            this.h[2] = h2;
            this.h[3] = h3;
            this.h[4] = h4;
            this.h[5] = h5;
            this.h[6] = h6;
            this.h[7] = h7;
            this.h[8] = h8;
            this.h[9] = h9;

            return this;
        }

        public poly1305 finish(byte[] mac, int macpos) {
            int[] g = new int[10];
            int c, mask, f, i;

            if (this.leftover != 0) {
                i = this.leftover;
                this.buffer[i++] = 1;
                for (; i < 16; i++) this.buffer[i] = 0;
                this.fin = 1;
                this.blocks(this.buffer, 0, 16);
            }

            c = this.h[1] >>> 13;
            this.h[1] &= 0x1fff;
            for (i = 2; i < 10; i++) {
                this.h[i] += c;
                c = this.h[i] >>> 13;
                this.h[i] &= 0x1fff;
            }
            this.h[0] += (c * 5);
            c = this.h[0] >>> 13;
            this.h[0] &= 0x1fff;
            this.h[1] += c;
            c = this.h[1] >>> 13;
            this.h[1] &= 0x1fff;
            this.h[2] += c;

            g[0] = this.h[0] + 5;
            c = g[0] >>> 13;
            g[0] &= 0x1fff;
            for (i = 1; i < 10; i++) {
                g[i] = this.h[i] + c;
                c = g[i] >>> 13;
                g[i] &= 0x1fff;
            }
            g[9] -= (1 << 13);
            g[9] &= 0xffff;

            /*
                        backport from tweetnacl-fast.js https://github.com/dchest/tweetnacl-js/releases/tag/v0.14.3
                        <<<
                        "The issue was not properly detecting if st->h was >= 2^130 - 5,
                        coupled with [testing mistake] not catching the failure.
                        The chance of the bug affecting anything in the real world is essentially zero luckily,
                        but it's good to have it fixed."
                        >>>
                        */
            ///change mask = (g[9] >>> ((2 * 8) - 1)) - 1; to as
            mask = (c ^ 1) - 1;
            mask &= 0xffff;
            ///////////////////////////////////////

            for (i = 0; i < 10; i++) g[i] &= mask;
            mask = ~mask;
            for (i = 0; i < 10; i++) this.h[i] = (this.h[i] & mask) | g[i];

            this.h[0] = ((this.h[0]) | (this.h[1] << 13)) & 0xffff;
            this.h[1] = ((this.h[1] >>> 3) | (this.h[2] << 10)) & 0xffff;
            this.h[2] = ((this.h[2] >>> 6) | (this.h[3] << 7)) & 0xffff;
            this.h[3] = ((this.h[3] >>> 9) | (this.h[4] << 4)) & 0xffff;
            this.h[4] = ((this.h[4] >>> 12) | (this.h[5] << 1) | (this.h[6] << 14)) & 0xffff;
            this.h[5] = ((this.h[6] >>> 2) | (this.h[7] << 11)) & 0xffff;
            this.h[6] = ((this.h[7] >>> 5) | (this.h[8] << 8)) & 0xffff;
            this.h[7] = ((this.h[8] >>> 8) | (this.h[9] << 5)) & 0xffff;

            f = this.h[0] + this.pad[0];
            this.h[0] = f & 0xffff;
            for (i = 1; i < 8; i++) {
                f = (((this.h[i] + this.pad[i]) | 0) + (f >>> 16)) | 0;
                this.h[i] = f & 0xffff;
            }

            mac[macpos + 0] = (byte) ((this.h[0] >>> 0) & 0xff);
            mac[macpos + 1] = (byte) ((this.h[0] >>> 8) & 0xff);
            mac[macpos + 2] = (byte) ((this.h[1] >>> 0) & 0xff);
            mac[macpos + 3] = (byte) ((this.h[1] >>> 8) & 0xff);
            mac[macpos + 4] = (byte) ((this.h[2] >>> 0) & 0xff);
            mac[macpos + 5] = (byte) ((this.h[2] >>> 8) & 0xff);
            mac[macpos + 6] = (byte) ((this.h[3] >>> 0) & 0xff);
            mac[macpos + 7] = (byte) ((this.h[3] >>> 8) & 0xff);
            mac[macpos + 8] = (byte) ((this.h[4] >>> 0) & 0xff);
            mac[macpos + 9] = (byte) ((this.h[4] >>> 8) & 0xff);
            mac[macpos + 10] = (byte) ((this.h[5] >>> 0) & 0xff);
            mac[macpos + 11] = (byte) ((this.h[5] >>> 8) & 0xff);
            mac[macpos + 12] = (byte) ((this.h[6] >>> 0) & 0xff);
            mac[macpos + 13] = (byte) ((this.h[6] >>> 8) & 0xff);
            mac[macpos + 14] = (byte) ((this.h[7] >>> 0) & 0xff);
            mac[macpos + 15] = (byte) ((this.h[7] >>> 8) & 0xff);

            return this;
        }

        public poly1305 update(byte[] m, int mpos, int bytes) {
            int i, want;

            if (this.leftover != 0) {
                want = (16 - this.leftover);
                if (want > bytes) want = bytes;
                for (i = 0; i < want; i++) this.buffer[this.leftover + i] = m[mpos + i];
                bytes -= want;
                mpos += want;
                this.leftover += want;
                if (this.leftover < 16) return this;
                this.blocks(buffer, 0, 16);
                this.leftover = 0;
            }

            if (bytes >= 16) {
                want = bytes - (bytes % 16);
                this.blocks(m, mpos, want);
                mpos += want;
                bytes -= want;
            }

            if (bytes != 0) {
                for (i = 0; i < bytes; i++) this.buffer[this.leftover + i] = m[mpos + i];
                this.leftover += bytes;
            }

            return this;
        }
    }

    private static int crypto_onetimeauth(byte[] out, final int outpos, byte[] m, final int mpos, int n, byte[] k) {
        poly1305 s = new poly1305(k);
        s.update(m, mpos, n);
        s.finish(out, outpos);

        /*String dbgt = "";
		for (int dbg = 0; dbg < out.length-outpos; dbg ++) dbgt += " "+out[dbg+outpos];
		Log.d(TAG, "crypto_onetimeauth -> "+dbgt);
		*/

        return 0;
    }

    private static int crypto_onetimeauth_verify(byte[] h, final int hoff, byte[] m, final int moff, int /*long*/n, byte[] k) {
        byte[] x = new byte[16];
        crypto_onetimeauth(x, 0, m, moff, n, k);
        return crypto_verify_16(h, hoff, x, 0);
    }

    public static int crypto_secretbox_open(byte[] m, byte[] c, int /*long*/d, byte[] n, byte[] k) {
        int i;
        byte[] x = new byte[32];
        if (d < 32) return -1;
        crypto_stream(x, 0, 32, n, k);
        if (crypto_onetimeauth_verify(c, 16, c, 32, d - 32, x) != 0) return -1;
        crypto_stream_xor(m, 0, c, 0, d, n, k);
        ///for (i = 0; i < 32; i++) m[i] = 0;
        return 0;
    }
}
