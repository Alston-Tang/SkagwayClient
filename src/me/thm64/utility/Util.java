package me.thm64.utility;

import java.util.Date;

public class Util {
    public final static int UINT16_LEN = 16;
    public final static int UINT32_LEN = 32;
    public final static int BYTE_LEN = Byte.SIZE;
    public final static int BYTE_PER_UINT32 = UINT32_LEN / BYTE_LEN;
    public final static int BYTE_PER_UINT16 = UINT16_LEN / BYTE_LEN;



    public static long uint32ToLong(byte[] n) {
        return uint32ToLong(n, 0);
    }

    public static long uint32ToLong(byte[] n, int offset) {
        long result = 0;
        for (int i = offset; i < offset + BYTE_PER_UINT32; i++) {
            result <<= 8;
            result |= (n[i] & 0xFF);
        }
        return result;
    }


    public static int uint16ToInt(byte[] n) {
        return uint16ToInt(n, 0);
    }

    public static int uint16ToInt(byte[] n, int offset) {
        int result = 0;
        for (int i = offset; i < offset + BYTE_PER_UINT16; i++) {
            result <<= 8;
            result |= (n[i] & 0xFF);
        }
        return result;
    }


    public static byte[] longToUint32(long n) {
        byte[] rv = new byte[BYTE_PER_UINT32];
        longToUint32(n, rv, 0);
        return rv;
    }

    public static void longToUint32(long n, byte[] uint32, int offset) {
        if (n >= 4294967296L || uint32.length < BYTE_PER_UINT32) {
            System.err.println("Warn: longToUint32: value can not fit into uint32");
            //System.exit(-1);
        }
        for (int i = offset + BYTE_PER_UINT32 - 1; i >= offset; i--) {
            uint32[i] = (byte)(n & 0xFF);
            n >>= BYTE_LEN;
        }
    }


    public static byte[] intToUint16(long n) {
        byte[] rv = new byte[BYTE_PER_UINT16];
        intToUint16(n, rv, 0);
        return rv;
    }

    public static void intToUint16(long n, byte[] uint16, int offset) {
        if (n >= 65536 || uint16.length < BYTE_PER_UINT16) {
            System.err.println("Warn: longToUint16: value can not fit into uint16");
        }
        for (int i = offset + BYTE_PER_UINT16 - 1; i >= offset; i--) {
            uint16[i] = (byte)(n & 0xFF);
            n >>= BYTE_LEN;
        }
    }

    public static int getByteHash(byte[] data, int offset, int length) {
        final int p = 16777619;
        int hash = -2128831035;
        for (int i = offset; i < offset + length; i++) {
            hash = (hash ^ data[i]) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash;
    }

    public static long curTimeUint32() {
        return new Date().getTime() & 0xffffffffL;
    }
}
