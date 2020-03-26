package com.pusher.client.util;

import static java.util.Arrays.fill;

// copied from: https://stackoverflow.com/a/4265472/501940 and improved (naming, char validation)
public class Base64 {

    private final static char[] CHAR_INDEX_TABLE =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private static int[] charToIndexSparseMappingArray = new int[128];

    static {
        fill(charToIndexSparseMappingArray, -1);
        for (int i = 0; i < CHAR_INDEX_TABLE.length; i++) {
            charToIndexSparseMappingArray[CHAR_INDEX_TABLE[i]] = i;
        }
    }

    private static int toInt(char character) {
        int retVal = charToIndexSparseMappingArray[character];
        if (retVal == -1) throw new IllegalArgumentException("invalid char: " + character);
        return retVal;
    }

    public static byte[] decode(String base64String) {
        int paddingSize = base64String.endsWith("==") ? 2 : base64String.endsWith("=") ? 1 : 0;
        byte[] buffer = new byte[base64String.length() * 3 / 4 - paddingSize];
        int mask = 0xFF;
        int index = 0;
        for (int i = 0; i < base64String.length(); i += 4) {
            int c0 = toInt(base64String.charAt(i));
            int c1 = toInt(base64String.charAt(i + 1));
            buffer[index++] = (byte) (((c0 << 2) | (c1 >> 4)) & mask);
            if (index >= buffer.length) {
                return buffer;
            }
            int c2 = toInt(base64String.charAt(i + 2));
            buffer[index++] = (byte) (((c1 << 4) | (c2 >> 2)) & mask);
            if (index >= buffer.length) {
                return buffer;
            }
            int c3 = toInt(base64String.charAt(i + 3));
            buffer[index++] = (byte) (((c2 << 6) | c3) & mask);
        }
        return buffer;
    }
}