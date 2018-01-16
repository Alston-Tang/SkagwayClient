package me.thm64.protocol;

import me.thm64.utility.Util;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import static org.testng.Assert.*;

public class KeepAliveSTest {
    private byte[] capData;
    @BeforeMethod
    public void setUp() throws Exception {
        File cap = new File("test/me/thm64/capture/KeepAliveS.cap");
        FileInputStream in = new FileInputStream(cap);
        capData = in.readAllBytes();
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }


    @Test
    public void testKeepAliveS() throws Exception {
        long clientTime = 0xFF535726L;
        long serverTime = 0xFF535725L;
        KeepAliveS message= new KeepAliveS(clientTime, serverTime);

        Assert.assertEquals(message.getClientTime(), clientTime);
        Assert.assertEquals(message.getServerTime(), serverTime);
    }

    @Test
    public void testGetClientTime() throws Exception {
        byte[] data = {(byte)0xff, (byte)0x5a, (byte)0x86, (byte)0xc0, (byte)0x00, (byte)0x00, (byte)0xf6, (byte)0x00};
        KeepAliveS message = new KeepAliveS(data);
        Assert.assertEquals(message.getClientTime(), 4284122816L);
    }

    @Test
    public void testGetServerTime() throws Exception {
        byte[] data = {(byte)0xff, (byte)0x5a, (byte)0x86, (byte)0xc0, (byte)0x00, (byte)0x00, (byte)0xf6, (byte)0x00};
        KeepAliveS message = new KeepAliveS(data);
        Assert.assertEquals(message.getServerTime(), 62976L);
    }

    @Test
    public void testGetTimeOffset() throws Exception {
        long curTime = Util.curTimeUint32();
        KeepAliveS message = new KeepAliveS(curTime - 200, curTime - 100 + 1000);
        Assert.assertEquals(message.getTimeOffset(curTime), -1000);
    }

}