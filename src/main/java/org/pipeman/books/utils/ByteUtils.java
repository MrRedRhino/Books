package org.pipeman.books.utils;

import java.io.BufferedInputStream;
import java.io.IOException;

public class ByteUtils {
    public static byte[] longToBytes(long l) {
        return new byte[]{
                (byte) l, (byte) (l >> 8), (byte) (l >> 16), (byte) (l >> 24), (byte) (l >> 32),
                (byte) (l >> 40), (byte) (l >> 48), (byte) (l >> 56)
        };
    }

    public static long bytesToLong(byte[] input) {
        return (long) input[7] << 56
               | ((long) input[6] & 0xff) << 48
               | ((long) input[5] & 0xff) << 40
               | ((long) input[4] & 0xff) << 32
               | ((long) input[3] & 0xff) << 24
               | ((long) input[2] & 0xff) << 16
               | ((long) input[1] & 0xff) << 8
               | ((long) input[0] & 0xff);
    }

    public static byte[] intToBytes(int i) {
        return new byte[]{
                (byte) i, (byte) (i >> 8), (byte) (i >> 16), (byte) (i >> 24)
        };
    }

    public static int bytesToInt(byte[] input) {
        return (int) input[3] << 24
               | ((int) input[2] & 0xff) << 16
               | ((int) input[1] & 0xff) << 8
               | ((int) input[0] & 0xff);
    }

    public static byte[] stringToBytes(String s) {
        byte[] bytes = new byte[s.length() + 1];
        for (int i = 0; i < s.length(); i++) bytes[i] = (byte) s.charAt(i);
        bytes[bytes.length - 1] = 0;
        return bytes;
    }

    public static String bytesToString(byte[] input) {
        StringBuilder sb = new StringBuilder();
        for (byte b : input) {
            if (b == 0) break;
            sb.append((char) b);
        }
        return sb.toString();
    }

    public static String readString(BufferedInputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = is.read()) != 0) sb.append((char) b);
        return sb.toString();
    }
}
