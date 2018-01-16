package me.thm64.utility;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.*;

public class UtilTest {

    @BeforeMethod
    public void setUp() throws Exception {
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testUint32ToLong() throws Exception {
        byte[] uint32_1 = {(byte)0xff, (byte)0x5a, (byte)0x86, (byte)0xc0};
        Assert.assertEquals(Util.uint32ToLong(uint32_1), 4284122816L);
        byte[] uint32_2 = {(byte)0x00, (byte)0x00, (byte)0xf6, (byte)0x00};
        Assert.assertEquals(Util.uint32ToLong(uint32_2), 62976L);
    }

    @Test
    public void testUint32ToLong1() throws Exception {
        byte[] uint32_1 = {(byte)0xf5, (byte)0xff, (byte)0x5a, (byte)0x86, (byte)0xc0};
        Assert.assertEquals(Util.uint32ToLong(uint32_1, 1), 4284122816L);
        Assert.assertEquals(Util.uint32ToLong(uint32_1, 0), 4127152774L);
    }

    @Test
    public void testUint16ToInt() throws Exception {
        byte[] uint16_1 = {(byte)0xfc, (byte)0xcf};
        Assert.assertEquals(Util.uint16ToInt(uint16_1), 64719);
        byte[] uint16_2 = {(byte)0x00, (byte)0x00};
        Assert.assertEquals(Util.uint16ToInt(uint16_2), 0);
    }

    @Test
    public void testUint16ToInt1() throws Exception {
        byte[] uint16_1 = {(byte)0xff, (byte)0x5a, (byte)0x86, (byte)0xc0};
        Assert.assertEquals(Util.uint16ToInt(uint16_1, 0), 65370);
        Assert.assertEquals(Util.uint16ToInt(uint16_1, 2), 34496);
    }

    @Test
    public void testLongToUint32() throws Exception {
        byte[] expected = {(byte)0xFF, (byte)0xFF, (byte)0xF3, (byte)0x55};
        long input = 4294964053L;
        byte[] res = Util.longToUint32(input);
        Assert.assertEquals(expected, res);

        expected[0] = 0;
        input = 16773973L;
        res = Util.longToUint32(input);
        Assert.assertEquals(expected, res);

    }

    @Test
    public void testLongToUint321() throws Exception {
        byte[] expected = {(byte)0xFF, (byte)0xFF, (byte)0xF3, (byte)0x55};
        long input = 4294964053L;
        byte[] buffer = new byte[Util.BYTE_PER_UINT32 + 1];
        Util.longToUint32(input, buffer, 1);

        for (int i = 0; i < Util.BYTE_PER_UINT32; i++) {
            Assert.assertEquals(expected[i], buffer[i + 1]);
        }
    }


    @Test
    public void testIntToUint16() throws Exception {
        byte[] expected = {(byte)0xFF, (byte)0xFF};
        int input = 65535;
        byte[] res = Util.intToUint16(input);
        Assert.assertEquals(expected, res);

        expected[0] = 0;
        input = 255;
        res = Util.intToUint16(input);
        Assert.assertEquals(expected, res);
    }

    @Test
    public void testIntToUint161() throws Exception {
        byte[] expected = {(byte)0xFF, (byte)0xFF};
        int input = 65535;
        byte[] buffer = new byte[Util.BYTE_PER_UINT16 + 1];
        Util.intToUint16(input, buffer, 1);

        for (int i = 0; i < Util.BYTE_PER_UINT16; i++) {
            Assert.assertEquals(expected[i], buffer[i + 1]);
        }
    }

    @Test
    public void testGetByteHash() throws Exception {
        byte[] data1 = {(byte)0xFF, (byte)0xFF, (byte)0xF3, (byte)0x55};
        byte[] data2 = {(byte)0xFF, (byte)0xFF, (byte)0xF3, (byte)0x55, (byte)0x63};

        Assert.assertEquals(Util.getByteHash(data1, 0, 4), Util.getByteHash(data2, 0, 4));
        Assert.assertEquals(Util.getByteHash(data1, 2, 2), Util.getByteHash(data2, 2, 2));
        Assert.assertNotEquals(Util.getByteHash(data1, 0, 4), Util.getByteHash(data2, 1, 4));
        Assert.assertNotEquals(Util.getByteHash(data1, 0, 4), Util.getByteHash(data2, 0, 5));
    }

    @Test
    public void testCurTimeUint32() throws Exception {
        long curTime = new Date().getTime();
        Assert.assertEquals(Util.curTimeUint32(), Util.uint32ToLong(Util.longToUint32(curTime)));
    }

}