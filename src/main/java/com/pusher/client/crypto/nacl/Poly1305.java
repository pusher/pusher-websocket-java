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

public class Poly1305 {
    public static final int TAG_SIZE = 16;

    private static final double ALPHAM_80 = 0.00000000558793544769287109375d;
    private static final double ALPHAM_48 = 24.0d;
    private static final double ALPHAM_16 = 103079215104.0d;
    private static final double ALPHA_0 = 6755399441055744.0d;
    private static final double ALPHA_18 = 1770887431076116955136.0d;
    private static final double ALPHA_32 = 29014219670751100192948224.0d;
    private static final double ALPHA_50 = 7605903601369376408980219232256.0d;
    private static final double ALPHA_64 = 124615124604835863084731911901282304.0d;
    private static final double ALPHA_82 = 32667107224410092492483962313449748299776.0d;
    private static final double ALPHA_96 = 535217884764734955396857238543560676143529984.0d;
    private static final double ALPHA_112 = 35076039295941670036888435985190792471742381031424.0d;
    private static final double ALPHA_130 = 9194973245195333150150082162901855101712434733101613056.0d;
    private static final double SCALE = 0.0000000000000000000000000000000000000036734198463196484624023016788195177431833298649127735047148490821200539357960224151611328125d;
    private static final double OFFSET_0 = 6755408030990331.0d;
    private static final double OFFSET_1 = 29014256564239239022116864.0d;
    private static final double OFFSET_2 = 124615283061160854719918951570079744.0d;
    private static final double OFFSET_3 = 535219245894202480694386063513315216128475136.0d;

    private static long uint32(long x) {
        return 0xFFFFFFFF & x;
    }

    private static double longBitsToDouble(long bits) {
        int s = ((bits >> 63) == 0) ? 1 : -1;
        int e = (int) ((bits >> 52) & 0x7ffL);
        long m = (e == 0) ?
                (bits & 0xfffffffffffffL) << 1 :
                (bits & 0xfffffffffffffL) | 0x10000000000000L;
        return (double) s * (double) m * Math.pow(2, e - 1075);
    }

    public static boolean verify(byte[] mac, byte[] m, byte[] key) {
        byte[] tmp = sum(m, key);
        //Util.printHex("tmp", tmp);
        //Util.printHex("mac", mac);
        return Subtle.constantTimeCompare(tmp, mac);
    }

    // Sum generates an authenticator for m using a one-time key and puts the
    // 16-byte result into out. Authenticating two different messages with the same
    // key allows an attacker to forge messages at will.
    @SuppressWarnings("SuspiciousNameCombination")
    public static byte[] sum(byte[] m, byte[] key) {
        byte[] r = key.clone();
        byte[] s = new byte[16];
        System.arraycopy(key, 16, s, 0, s.length);

        double y7;
        double y6;
        double y1;
        double y0;
        double y5;
        double y4;
        double x7;
        double x6;
        double x1;
        double x0;
        double y3;
        double y2;
        double x5;
        double r3lowx0;
        double x4;
        double r0lowx6;
        double x3;
        double r3highx0;
        double x2;
        double r0highx6;
        double r0lowx0;
        double sr1lowx6;
        double r0highx0;
        double sr1highx6;
        double sr3low;
        double r1lowx0;
        double sr2lowx6;
        double r1highx0;
        double sr2highx6;
        double r2lowx0;
        double sr3lowx6;
        double r2highx0;
        double sr3highx6;
        double r1highx4;
        double r1lowx4;
        double r0highx4;
        double r0lowx4;
        double sr3highx4;
        double sr3lowx4;
        double sr2highx4;
        double sr2lowx4;
        double r0lowx2;
        double r0highx2;
        double r1lowx2;
        double r1highx2;
        double r2lowx2;
        double r2highx2;
        double sr3lowx2;
        double sr3highx2;
        double z0;
        double z1;
        double z2;
        double z3;
        long m0;
        long m1;
        long m2;
        long m3;
        long m00;//uint32
        long m01;//uint32
        long m02;//uint32
        long m03;//uint32
        long m10;//uint32
        long m11;//uint32
        long m12;//uint32
        long m13;//uint32
        long m20;//uint32
        long m21;//uint32
        long m22;//uint32
        long m23;//uint32
        long m30;//uint32
        long m31;//uint32
        long m32;//uint32
        long m33;//uint64
        long lbelow2;//int32
        long lbelow3;//int32
        long lbelow4;//int32
        long lbelow5;//int32
        long lbelow6;//int32
        long lbelow7;//int32
        long lbelow8;//int32
        long lbelow9;//int32
        long lbelow10;//int32
        long lbelow11;//int32
        long lbelow12;//int32
        long lbelow13;//int32
        long lbelow14;//int32
        long lbelow15;//int32
        long s00;//uint32
        long s01;//uint32
        long s02;//uint32
        long s03;//uint32
        long s10;//uint32
        long s11;//uint32
        long s12;//uint32
        long s13;//uint32
        long s20;//uint32
        long s21;//uint32
        long s22;//uint32
        long s23;//uint32
        long s30;//uint32
        long s31;//uint32
        long s32;//uint32
        long s33;//uint32
        long bits32;//uint64
        long f;//uint64
        long f0;//uint64
        long f1;//uint64
        long f2;//uint64
        long f3;//uint64
        long f4;//uint64
        long g;//uint64
        long g0;//uint64
        long g1;//uint64
        long g2;//uint64
        long g3;//uint64
        long g4;//uint64

        long p = 0;

        int l = m.length;

        long r00 = 0xFF & r[0];
        long r01 = 0xFF & r[1];
        long r02 = 0xFF & r[2];
        long r0 = 2151;

        long r03 = 0xFF & r[3];
        r03 &= 15;
        r0 <<= 51;

        long r10 = 0xFF & r[4];
        r10 &= 252;
        r01 <<= 8;
        r0 += r00;

        long r11 = 0xFF & r[5];
        r02 <<= 16;
        r0 += r01;

        long r12 = 0xFF & r[6];
        r03 <<= 24;
        r0 += r02;

        long r13 = 0xFF & r[7];
        r13 &= 15;
        long r1 = 2215;
        r0 += r03;

        long d0 = r0;
        r1 <<= 51;
        long r2 = 2279;

        long r20 = 0xFF & r[8];
        r20 &= 252;
        r11 <<= 8;
        r1 += r10;

        long r21 = 0xFF & r[9];
        r12 <<= 16;
        r1 += r11;

        long r22 = 0xFF & r[10];
        r13 <<= 24;
        r1 += r12;

        long r23 = 0xFF & r[11];
        r23 &= 15;
        r2 <<= 51;
        r1 += r13;

        long d1 = r1;
        r21 <<= 8;
        r2 += r20;

        long r30 = 0xFF & r[12];
        r30 &= 252;
        r22 <<= 16;
        r2 += r21;

        long r31 = 0xFF & r[13];
        r23 <<= 24;
        r2 += r22;

        long r32 = 0xFF & r[14];
        r2 += r23;
        long r3 = 2343;

        long d2 = r2;
        r3 <<= 51;

        long r33 = 0xFF & r[15];
        r33 &= 15;
        r31 <<= 8;
        r3 += r30;

        r32 <<= 16;
        r3 += r31;

        r33 <<= 24;
        r3 += r32;

        r3 += r33;
        double h0 = ALPHA_32 - ALPHA_32;

        long d3 = r3;
        double h1 = ALPHA_32 - ALPHA_32;

        double h2 = ALPHA_32 - ALPHA_32;

        double h3 = ALPHA_32 - ALPHA_32;

        double h4 = ALPHA_32 - ALPHA_32;

        double r0low = Double.longBitsToDouble(d0);
        double h5 = ALPHA_32 - ALPHA_32;

        double r1low = longBitsToDouble(d1);
        double h6 = ALPHA_32 - ALPHA_32;

        double r2low = Double.longBitsToDouble(d2);
        double h7 = ALPHA_32 - ALPHA_32;

        r0low -= ALPHA_0;

        r1low -= ALPHA_32;

        r2low -= ALPHA_64;

        double r0high = r0low + ALPHA_18;

        double r3low = Double.longBitsToDouble(d3);

        double r1high = r1low + ALPHA_50;
        double sr1low = SCALE * r1low;

        double r2high = r2low + ALPHA_82;
        double sr2low = SCALE * r2low;

        r0high -= ALPHA_18;
        double r0high_stack = r0high;

        r3low -= ALPHA_96;

        r1high -= ALPHA_50;
        double r1high_stack = r1high;

        double sr1high = sr1low + ALPHAM_80;

        r0low -= r0high;

        r2high -= ALPHA_82;
        sr3low = SCALE * r3low;

        double sr2high = sr2low + ALPHAM_48;

        r1low -= r1high;
        double r1low_stack = r1low;

        sr1high -= ALPHAM_80;
        double sr1high_stack = sr1high;

        r2low -= r2high;
        double r2low_stack = r2low;

        sr2high -= ALPHAM_48;
        double sr2high_stack = sr2high;

        double r3high = r3low + ALPHA_112;
        double r0low_stack = r0low;

        sr1low -= sr1high;
        double sr1low_stack = sr1low;

        double sr3high = sr3low + ALPHAM_16;
        double r2high_stack = r2high;

        sr2low -= sr2high;
        double sr2low_stack = sr2low;

        r3high -= ALPHA_112;
        double r3high_stack = r3high;

        sr3high -= ALPHAM_16;
        double sr3high_stack = sr3high;

        r3low -= r3high;
        double r3low_stack = r3low;

        sr3low -= sr3high;
        double sr3low_stack = sr3low;


        if (!(l < 16)) {
            m00 = 0xFF & m[(int) p];
            m0 = 2151;

            m0 <<= 51;
            m1 = 2215;
            m01 = 0xFF & m[(int) p + 1];

            m1 <<= 51;
            m2 = 2279;
            m02 = 0xFF & m[(int) p + 2];

            m2 <<= 51;
            m3 = 2343;
            m03 = 0xFF & (m[(int) p + 3]);

            m10 = 0xFF & (m[(int) p + 4]);
            m01 <<= 8;
            m0 += m00;

            m11 = 0xFF & (m[(int) p + 5]);
            m02 <<= 16;
            m0 += m01;

            m12 = 0xFF & (m[(int) p + 6]);
            m03 <<= 24;
            m0 += m02;

            m13 = 0xFF & (m[(int) p + 7]);
            m3 <<= 51;
            m0 += m03;

            m20 = 0xFF & (m[(int) p + 8]);
            m11 <<= 8;
            m1 += m10;

            m21 = 0xFF & (m[(int) p + 9]);
            m12 <<= 16;
            m1 += m11;

            m22 = 0xFF & (m[(int) p + 10]);
            m13 <<= 24;
            m1 += m12;

            m23 = 0xFF & (m[(int) p + 11]);
            m1 += m13;

            m30 = 0xFF & (m[(int) p + 12]);
            m21 <<= 8;
            m2 += m20;

            m31 = 0xFF & (m[(int) p + 13]);
            m22 <<= 16;
            m2 += m21;

            m32 = 0xFF & (m[(int) p + 14]);
            m23 <<= 24;
            m2 += m22;

            m33 = 0xFF & (m[(int) p + 15]);
            m2 += m23;

            d0 = m0;
            m31 <<= 8;
            m3 += m30;

            d1 = m1;
            m32 <<= 16;
            m3 += m31;

            d2 = m2;
            m33 += 256;

            m33 <<= 24;
            m3 += m32;

            m3 += m33;
            d3 = m3;

            p += 16;
            l -= 16;

            z0 = Double.longBitsToDouble(d0);

            z1 = Double.longBitsToDouble(d1);

            z2 = Double.longBitsToDouble(d2);

            z3 = Double.longBitsToDouble(d3);

            z0 -= ALPHA_0;

            z1 -= ALPHA_32;

            z2 -= ALPHA_64;

            z3 -= ALPHA_96;

            h0 += z0;

            h1 += z1;

            h3 += z2;

            h5 += z3;

            while (l >= 16) {
                //multiplyaddatleast16bytes:

                m2 = 2279;
                m20 = 0xFF & (m[(int) p + 8]);
                y7 = h7 + ALPHA_130;

                m2 <<= 51;
                m3 = 2343;
                m21 = 0xFF & (m[(int) p + 9]);
                y6 = h6 + ALPHA_130;

                m3 <<= 51;
                m0 = 2151;
                m22 = 0xFF & (m[(int) p + 10]);
                y1 = h1 + ALPHA_32;

                m0 <<= 51;
                m1 = 2215;
                m23 = 0xFF & (m[(int) p + 11]);
                y0 = h0 + ALPHA_32;

                m1 <<= 51;
                m30 = 0xFF & (m[(int) p + 12]);
                y7 -= ALPHA_130;

                m21 <<= 8;
                m2 += m20;
                m31 = 0xFF & (m[(int) p + 13]);
                y6 -= ALPHA_130;

                m22 <<= 16;
                m2 += m21;
                m32 = 0xFF & (m[(int) p + 14]);
                y1 -= ALPHA_32;

                m23 <<= 24;
                m2 += m22;
                m33 = 0xFF & (m[(int) p + 15]);
                y0 -= ALPHA_32;

                m2 += m23;
                m00 = 0xFF & (m[(int) p]);
                y5 = h5 + ALPHA_96;

                m31 <<= 8;
                m3 += m30;
                m01 = 0xFF & (m[(int) p + 1]);
                y4 = h4 + ALPHA_96;

                m32 <<= 16;
                m02 = 0xFF & (m[(int) p + 2]);
                x7 = h7 - y7;
                y7 *= SCALE;

                m33 += 256;
                m03 = 0xFF & (m[(int) p + 3]);
                x6 = h6 - y6;
                y6 *= SCALE;

                m33 <<= 24;
                m3 += m31;
                m10 = 0xFF & (m[(int) p + 4]);
                x1 = h1 - y1;

                m01 <<= 8;
                m3 += m32;
                m11 = 0xFF & (m[(int) p + 5]);
                x0 = h0 - y0;

                m3 += m33;
                m0 += m00;
                m12 = 0xFF & (m[(int) p + 6]);
                y5 -= ALPHA_96;

                m02 <<= 16;
                m0 += m01;
                m13 = 0xFF & (m[(int) p + 7]);
                y4 -= ALPHA_96;

                m03 <<= 24;
                m0 += m02;
                d2 = m2;
                x1 += y7;

                m0 += m03;
                d3 = m3;
                x0 += y6;

                m11 <<= 8;
                m1 += m10;
                d0 = m0;
                x7 += y5;

                m12 <<= 16;
                m1 += m11;
                x6 += y4;

                m13 <<= 24;
                m1 += m12;
                y3 = h3 + ALPHA_64;

                m1 += m13;
                d1 = m1;
                y2 = h2 + ALPHA_64;

                x0 += x1;

                x6 += x7;

                y3 -= ALPHA_64;
                r3low = r3low_stack;

                y2 -= ALPHA_64;
                r0low = r0low_stack;

                x5 = h5 - y5;
                r3lowx0 = r3low * x0;
                r3high = r3high_stack;

                x4 = h4 - y4;
                r0lowx6 = r0low * x6;
                r0high = r0high_stack;

                x3 = h3 - y3;
                r3highx0 = r3high * x0;
                sr1low = sr1low_stack;

                x2 = h2 - y2;
                r0highx6 = r0high * x6;
                sr1high = sr1high_stack;

                x5 += y3;
                r0lowx0 = r0low * x0;
                r1low = r1low_stack;

                h6 = r3lowx0 + r0lowx6;
                sr1lowx6 = sr1low * x6;
                r1high = r1high_stack;

                x4 += y2;
                r0highx0 = r0high * x0;
                sr2low = sr2low_stack;

                h7 = r3highx0 + r0highx6;
                sr1highx6 = sr1high * x6;
                sr2high = sr2high_stack;

                x3 += y1;
                r1lowx0 = r1low * x0;
                r2low = r2low_stack;

                h0 = r0lowx0 + sr1lowx6;
                sr2lowx6 = sr2low * x6;
                r2high = r2high_stack;

                x2 += y0;
                r1highx0 = r1high * x0;
                sr3low = sr3low_stack;

                h1 = r0highx0 + sr1highx6;
                sr2highx6 = sr2high * x6;
                sr3high = sr3high_stack;

                x4 += x5;
                r2lowx0 = r2low * x0;
                z2 = Double.longBitsToDouble(d2);

                h2 = r1lowx0 + sr2lowx6;
                sr3lowx6 = sr3low * x6;

                x2 += x3;
                r2highx0 = r2high * x0;
                z3 = Double.longBitsToDouble(d3);

                h3 = r1highx0 + sr2highx6;
                sr3highx6 = sr3high * x6;

                r1highx4 = r1high * x4;
                z2 -= ALPHA_64;

                h4 = r2lowx0 + sr3lowx6;
                r1lowx4 = r1low * x4;

                r0highx4 = r0high * x4;
                z3 -= ALPHA_96;

                h5 = r2highx0 + sr3highx6;
                r0lowx4 = r0low * x4;

                h7 += r1highx4;
                sr3highx4 = sr3high * x4;

                h6 += r1lowx4;
                sr3lowx4 = sr3low * x4;

                h5 += r0highx4;
                sr2highx4 = sr2high * x4;

                h4 += r0lowx4;
                sr2lowx4 = sr2low * x4;

                h3 += sr3highx4;
                r0lowx2 = r0low * x2;

                h2 += sr3lowx4;
                r0highx2 = r0high * x2;

                h1 += sr2highx4;
                r1lowx2 = r1low * x2;

                h0 += sr2lowx4;
                r1highx2 = r1high * x2;

                h2 += r0lowx2;
                r2lowx2 = r2low * x2;

                h3 += r0highx2;
                r2highx2 = r2high * x2;

                h4 += r1lowx2;
                sr3lowx2 = sr3low * x2;

                h5 += r1highx2;
                sr3highx2 = sr3high * x2;

                p += 16;
                l -= 16;
                h6 += r2lowx2;

                h7 += r2highx2;

                z1 = Double.longBitsToDouble(d1);
                h0 += sr3lowx2;

                z0 = Double.longBitsToDouble(d0);
                h1 += sr3highx2;

                z1 -= ALPHA_32;

                z0 -= ALPHA_0;

                h5 += z3;

                h3 += z2;

                h1 += z1;

                h0 += z0;

            }

            // multiplyaddatmost15bytes:
            y7 = h7 + ALPHA_130;

            y6 = h6 + ALPHA_130;

            y1 = h1 + ALPHA_32;

            y0 = h0 + ALPHA_32;

            y7 -= ALPHA_130;

            y6 -= ALPHA_130;

            y1 -= ALPHA_32;

            y0 -= ALPHA_32;

            y5 = h5 + ALPHA_96;

            y4 = h4 + ALPHA_96;

            x7 = h7 - y7;
            y7 *= SCALE;

            x6 = h6 - y6;
            y6 *= SCALE;

            x1 = h1 - y1;

            x0 = h0 - y0;

            y5 -= ALPHA_96;

            y4 -= ALPHA_96;

            x1 += y7;

            x0 += y6;

            x7 += y5;

            x6 += y4;

            y3 = h3 + ALPHA_64;

            y2 = h2 + ALPHA_64;

            x0 += x1;

            x6 += x7;

            y3 -= ALPHA_64;
            r3low = r3low_stack;

            y2 -= ALPHA_64;
            r0low = r0low_stack;

            x5 = h5 - y5;
            r3lowx0 = r3low * x0;
            r3high = r3high_stack;

            x4 = h4 - y4;
            r0lowx6 = r0low * x6;
            r0high = r0high_stack;

            x3 = h3 - y3;
            r3highx0 = r3high * x0;
            sr1low = sr1low_stack;

            x2 = h2 - y2;
            r0highx6 = r0high * x6;
            sr1high = sr1high_stack;

            x5 += y3;
            r0lowx0 = r0low * x0;
            r1low = r1low_stack;

            h6 = r3lowx0 + r0lowx6;
            sr1lowx6 = sr1low * x6;
            r1high = r1high_stack;

            x4 += y2;
            r0highx0 = r0high * x0;
            sr2low = sr2low_stack;

            h7 = r3highx0 + r0highx6;
            sr1highx6 = sr1high * x6;
            sr2high = sr2high_stack;

            x3 += y1;
            r1lowx0 = r1low * x0;
            r2low = r2low_stack;

            h0 = r0lowx0 + sr1lowx6;
            sr2lowx6 = sr2low * x6;
            r2high = r2high_stack;

            x2 += y0;
            r1highx0 = r1high * x0;
            sr3low = sr3low_stack;

            h1 = r0highx0 + sr1highx6;
            sr2highx6 = sr2high * x6;
            sr3high = sr3high_stack;

            x4 += x5;
            r2lowx0 = r2low * x0;

            h2 = r1lowx0 + sr2lowx6;
            sr3lowx6 = sr3low * x6;

            x2 += x3;
            r2highx0 = r2high * x0;

            h3 = r1highx0 + sr2highx6;
            sr3highx6 = sr3high * x6;

            r1highx4 = r1high * x4;

            h4 = r2lowx0 + sr3lowx6;
            r1lowx4 = r1low * x4;

            r0highx4 = r0high * x4;

            h5 = r2highx0 + sr3highx6;
            r0lowx4 = r0low * x4;

            h7 += r1highx4;
            sr3highx4 = sr3high * x4;

            h6 += r1lowx4;
            sr3lowx4 = sr3low * x4;

            h5 += r0highx4;
            sr2highx4 = sr2high * x4;

            h4 += r0lowx4;
            sr2lowx4 = sr2low * x4;

            h3 += sr3highx4;
            r0lowx2 = r0low * x2;

            h2 += sr3lowx4;
            r0highx2 = r0high * x2;

            h1 += sr2highx4;
            r1lowx2 = r1low * x2;

            h0 += sr2lowx4;
            r1highx2 = r1high * x2;

            h2 += r0lowx2;
            r2lowx2 = r2low * x2;

            h3 += r0highx2;
            r2highx2 = r2high * x2;

            h4 += r1lowx2;
            sr3lowx2 = sr3low * x2;

            h5 += r1highx2;
            sr3highx2 = sr3high * x2;

            h6 += r2lowx2;

            h7 += r2highx2;

            h0 += sr3lowx2;

            h1 += sr3highx2;
        }

        // addatmost15bytes:

        if (l > 0) {
            lbelow2 = l - 2;

            lbelow3 = l - 3;

            lbelow2 >>= 31;
            lbelow4 = l - 4;

            m00 = 0xFF & (m[(int) p]);
            lbelow3 >>= 31;
            p += lbelow2;

            m01 = 0xFF & (m[(int) p + 1]);
            lbelow4 >>= 31;
            p += lbelow3;

            m02 = 0xFF & (m[(int) p + 2]);
            p += lbelow4;
            m0 = 2151;

            m03 = 0xFF & (m[(int) p + 3]);
            m0 <<= 51;
            m1 = 2215;

            m0 += m00;
            m01 &= ~uint32(lbelow2);

            m02 &= ~uint32(lbelow3);
            m01 -= uint32(lbelow2);

            m01 <<= 8;
            m03 &= ~uint32(lbelow4);

            m0 += m01;
            lbelow2 -= lbelow3;

            m02 += uint32(lbelow2);
            lbelow3 -= lbelow4;

            m02 <<= 16;
            m03 += uint32(lbelow3);

            m03 <<= 24;
            m0 += m02;

            m0 += m03;
            lbelow5 = l - 5;

            lbelow6 = l - 6;
            lbelow7 = l - 7;

            lbelow5 >>= 31;
            lbelow8 = l - 8;

            lbelow6 >>= 31;
            p += lbelow5;

            m10 = 0xFF & (m[(int) p + 4]);
            lbelow7 >>= 31;
            p += lbelow6;

            m11 = 0xFF & (m[(int) p + 5]);
            lbelow8 >>= 31;
            p += lbelow7;

            m12 = 0xFF & (m[(int) p + 6]);
            m1 <<= 51;
            p += lbelow8;

            m13 = 0xFF & (m[(int) p + 7]);
            m10 &= ~uint32(lbelow5);
            lbelow4 -= lbelow5;

            m10 += uint32(lbelow4);
            lbelow5 -= lbelow6;

            m11 &= ~uint32(lbelow6);
            m11 += uint32(lbelow5);

            m11 <<= 8;
            m1 += m10;

            m1 += m11;
            m12 &= ~uint32(lbelow7);

            lbelow6 -= lbelow7;
            m13 &= ~uint32(lbelow8);

            m12 += uint32(lbelow6);
            lbelow7 -= lbelow8;

            m12 <<= 16;
            m13 += uint32(lbelow7);

            m13 <<= 24;
            m1 += m12;

            m1 += m13;
            m2 = 2279;

            lbelow9 = l - 9;
            m3 = 2343;

            lbelow10 = l - 10;
            lbelow11 = l - 11;

            lbelow9 >>= 31;
            lbelow12 = l - 12;

            lbelow10 >>= 31;
            p += lbelow9;

            m20 = 0xFF & (m[(int) p + 8]);
            lbelow11 >>= 31;
            p += lbelow10;

            m21 = 0xFF & (m[(int) p + 9]);
            lbelow12 >>= 31;
            p += lbelow11;

            m22 = 0xFF & (m[(int) p + 10]);
            m2 <<= 51;
            p += lbelow12;

            m23 = 0xFF & (m[(int) p + 11]);
            m20 &= ~uint32(lbelow9);
            lbelow8 -= lbelow9;

            m20 += uint32(lbelow8);
            lbelow9 -= lbelow10;

            m21 &= ~uint32(lbelow10);
            m21 += uint32(lbelow9);

            m21 <<= 8;
            m2 += m20;

            m2 += m21;
            m22 &= ~uint32(lbelow11);

            lbelow10 -= lbelow11;
            m23 &= ~uint32(lbelow12);

            m22 += uint32(lbelow10);
            lbelow11 -= lbelow12;

            m22 <<= 16;
            m23 += uint32(lbelow11);

            m23 <<= 24;
            m2 += m22;

            m3 <<= 51;
            lbelow13 = l - 13;

            lbelow13 >>= 31;
            lbelow14 = l - 14;

            lbelow14 >>= 31;
            p += lbelow13;
            lbelow15 = l - 15;

            m30 = uint32(m[(int) p + 12]);
            lbelow15 >>= 31;
            p += lbelow14;

            m31 = 0xFF & (m[(int) p + 13]);
            p += lbelow15;
            m2 += m23;

            m32 = 0xFF & (m[(int) p + 14]);
            m30 &= ~uint32(lbelow13);
            lbelow12 -= lbelow13;

            m30 += uint32(lbelow12);
            lbelow13 -= lbelow14;

            m3 += m30;
            m31 &= ~uint32(lbelow14);

            m31 += uint32(lbelow13);
            m32 &= ~uint32(lbelow15);

            m31 <<= 8;
            lbelow14 -= lbelow15;

            m3 += m31;
            m32 += uint32(lbelow14);
            d0 = m0;

            m32 <<= 16;
            m33 = lbelow15 + 1;
            d1 = m1;

            m33 <<= 24;
            m3 += m32;
            d2 = m2;

            m3 += m33;
            d3 = m3;

            z3 = Double.longBitsToDouble(d3);

            z2 = Double.longBitsToDouble(d2);

            z1 = Double.longBitsToDouble(d1);

            z0 = Double.longBitsToDouble(d0);

            z3 -= ALPHA_96;

            z2 -= ALPHA_64;

            z1 -= ALPHA_32;

            z0 -= ALPHA_0;

            h5 += z3;

            h3 += z2;

            h1 += z1;

            h0 += z0;

            y7 = h7 + ALPHA_130;

            y6 = h6 + ALPHA_130;

            y1 = h1 + ALPHA_32;

            y0 = h0 + ALPHA_32;

            y7 -= ALPHA_130;

            y6 -= ALPHA_130;

            y1 -= ALPHA_32;

            y0 -= ALPHA_32;

            y5 = h5 + ALPHA_96;

            y4 = h4 + ALPHA_96;

            x7 = h7 - y7;
            y7 *= SCALE;

            x6 = h6 - y6;
            y6 *= SCALE;

            x1 = h1 - y1;

            x0 = h0 - y0;

            y5 -= ALPHA_96;

            y4 -= ALPHA_96;

            x1 += y7;

            x0 += y6;

            x7 += y5;

            x6 += y4;

            y3 = h3 + ALPHA_64;

            y2 = h2 + ALPHA_64;

            x0 += x1;

            x6 += x7;

            y3 -= ALPHA_64;
            r3low = r3low_stack;

            y2 -= ALPHA_64;
            r0low = r0low_stack;

            x5 = h5 - y5;
            r3lowx0 = r3low * x0;
            r3high = r3high_stack;

            x4 = h4 - y4;
            r0lowx6 = r0low * x6;
            r0high = r0high_stack;

            x3 = h3 - y3;
            r3highx0 = r3high * x0;
            sr1low = sr1low_stack;

            x2 = h2 - y2;
            r0highx6 = r0high * x6;
            sr1high = sr1high_stack;

            x5 += y3;
            r0lowx0 = r0low * x0;
            r1low = r1low_stack;

            h6 = r3lowx0 + r0lowx6;
            sr1lowx6 = sr1low * x6;
            r1high = r1high_stack;

            x4 += y2;
            r0highx0 = r0high * x0;
            sr2low = sr2low_stack;

            h7 = r3highx0 + r0highx6;
            sr1highx6 = sr1high * x6;
            sr2high = sr2high_stack;

            x3 += y1;
            r1lowx0 = r1low * x0;
            r2low = r2low_stack;

            h0 = r0lowx0 + sr1lowx6;
            sr2lowx6 = sr2low * x6;
            r2high = r2high_stack;

            x2 += y0;
            r1highx0 = r1high * x0;
            sr3low = sr3low_stack;

            h1 = r0highx0 + sr1highx6;
            sr2highx6 = sr2high * x6;
            sr3high = sr3high_stack;

            x4 += x5;
            r2lowx0 = r2low * x0;

            h2 = r1lowx0 + sr2lowx6;
            sr3lowx6 = sr3low * x6;

            x2 += x3;
            r2highx0 = r2high * x0;

            h3 = r1highx0 + sr2highx6;
            sr3highx6 = sr3high * x6;

            r1highx4 = r1high * x4;

            h4 = r2lowx0 + sr3lowx6;
            r1lowx4 = r1low * x4;

            r0highx4 = r0high * x4;

            h5 = r2highx0 + sr3highx6;
            r0lowx4 = r0low * x4;

            h7 += r1highx4;
            sr3highx4 = sr3high * x4;

            h6 += r1lowx4;
            sr3lowx4 = sr3low * x4;

            h5 += r0highx4;
            sr2highx4 = sr2high * x4;

            h4 += r0lowx4;
            sr2lowx4 = sr2low * x4;

            h3 += sr3highx4;
            r0lowx2 = r0low * x2;

            h2 += sr3lowx4;
            r0highx2 = r0high * x2;

            h1 += sr2highx4;
            r1lowx2 = r1low * x2;

            h0 += sr2lowx4;
            r1highx2 = r1high * x2;

            h2 += r0lowx2;
            r2lowx2 = r2low * x2;

            h3 += r0highx2;
            r2highx2 = r2high * x2;

            h4 += r1lowx2;
            sr3lowx2 = sr3low * x2;

            h5 += r1highx2;
            sr3highx2 = sr3high * x2;

            h6 += r2lowx2;

            h7 += r2highx2;

            h0 += sr3lowx2;

            h1 += sr3highx2;
        }

        //nomorebytes:

        y7 = h7 + ALPHA_130;

        y0 = h0 + ALPHA_32;

        y1 = h1 + ALPHA_32;

        y2 = h2 + ALPHA_64;

        y7 -= ALPHA_130;

        y3 = h3 + ALPHA_64;

        y4 = h4 + ALPHA_96;

        y5 = h5 + ALPHA_96;

        x7 = h7 - y7;
        y7 *= SCALE;

        y0 -= ALPHA_32;

        y1 -= ALPHA_32;

        y2 -= ALPHA_64;

        h6 += x7;

        y3 -= ALPHA_64;

        y4 -= ALPHA_96;

        y5 -= ALPHA_96;

        y6 = h6 + ALPHA_130;

        x0 = h0 - y0;

        x1 = h1 - y1;

        x2 = h2 - y2;

        y6 -= ALPHA_130;

        x0 += y7;

        x3 = h3 - y3;

        x4 = h4 - y4;

        x5 = h5 - y5;

        x6 = h6 - y6;

        y6 *= SCALE;

        x2 += y0;

        x3 += y1;

        x4 += y2;

        x0 += y6;

        x5 += y3;

        x6 += y4;

        x2 += x3;

        x0 += x1;

        x4 += x5;

        x6 += y5;

        x2 += OFFSET_1;
        d1 = Double.doubleToLongBits(x2);

        x0 += OFFSET_0;
        d0 = Double.doubleToLongBits(x0);

        x4 += OFFSET_2;
        d2 = Double.doubleToLongBits(x4);

        x6 += OFFSET_3;
        d3 = Double.doubleToLongBits(x6);

        f0 = d0;

        f1 = d1;
        bits32 = 0xFFFFFFFFFFFFFFFFL;

        f2 = d2;
        bits32 >>>= 32;

        f3 = d3;
        f = f0 >> 32;

        f0 &= bits32;
        f &= 255;

        f1 += f;
        g0 = f0 + 5;

        g = g0 >> 32;
        g0 &= bits32;

        f = f1 >> 32;
        f1 &= bits32;

        f &= 255;
        g1 = f1 + g;

        g = g1 >> 32;
        f2 += f;

        f = f2 >> 32;
        g1 &= bits32;

        f2 &= bits32;
        f &= 255;

        f3 += f;
        g2 = f2 + g;

        g = g2 >> 32;
        g2 &= bits32;

        f4 = f3 >> 32;
        f3 &= bits32;

        f4 &= 255;
        g3 = f3 + g;

        g = g3 >> 32;
        g3 &= bits32;

        g4 = f4 + g;

        g4 = g4 - 4;
        s00 = 0xFF & (s[0]);

        f = g4 >> 63;
        s01 = 0xFF & (s[1]);

        f0 &= f;
        g0 &= ~f;
        s02 = 0xFF & (s[2]);

        f1 &= f;
        f0 |= g0;
        s03 = 0xFF & (s[3]);

        g1 &= ~f;
        f2 &= f;
        s10 = 0xFF & (s[4]);

        f3 &= f;
        g2 &= ~f;
        s11 = 0xFF & (s[5]);

        g3 &= ~f;
        f1 |= g1;
        s12 = 0xFF & (s[6]);

        f2 |= g2;
        f3 |= g3;
        s13 = 0xFF & (s[7]);

        s01 <<= 8;
        f0 += s00;
        s20 = 0xFF & (s[8]);

        s02 <<= 16;
        f0 += s01;
        s21 = 0xFF & (s[9]);

        s03 <<= 24;
        f0 += s02;
        s22 = 0xFF & (s[10]);

        s11 <<= 8;
        f1 += s10;
        s23 = 0xFF & (s[11]);

        s12 <<= 16;
        f1 += s11;
        s30 = 0xFF & (s[12]);

        s13 <<= 24;
        f1 += (s12);
        s31 = 0xFF & s[13];

        f0 += (s03);
        f1 += (s13);
        s32 = 0xFF & (s[14]);

        s21 <<= 8;
        f2 += (s20);
        s33 = 0xFF & (s[15]);

        s22 <<= 16;
        f2 += (s21);

        s23 <<= 24;
        f2 += (s22);

        s31 <<= 8;
        f3 += (s30);

        s32 <<= 16;
        f3 += (s31);

        s33 <<= 24;
        f3 += (s32);

        f2 += (s23);
        f3 += s33;

        byte[] out = new byte[16];
        out[0] = (byte) (f0);
        f0 >>= 8;
        out[1] = (byte) (f0);
        f0 >>= 8;
        out[2] = (byte) (f0);
        f0 >>= 8;
        out[3] = (byte) (f0);
        f0 >>= 8;
        f1 += f0;

        out[4] = (byte) (f1);
        f1 >>= 8;
        out[5] = (byte) (f1);
        f1 >>= 8;
        out[6] = (byte) (f1);
        f1 >>= 8;
        out[7] = (byte) (f1);
        f1 >>= 8;
        f2 += f1;

        out[8] = (byte) (f2);
        f2 >>= 8;
        out[9] = (byte) (f2);
        f2 >>= 8;
        out[10] = (byte) (f2);
        f2 >>= 8;
        out[11] = (byte) (f2);
        f2 >>= 8;
        f3 += f2;

        out[12] = (byte) f3;
        f3 >>= 8;
        out[13] = (byte) f3;
        f3 >>= 8;
        out[14] = (byte) f3;
        f3 >>= 8;
        out[15] = (byte) f3;
        return out;
    }
}
