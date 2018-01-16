package me.thm64.protocol;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class KeepAliveCTest {
    private KeepAliveC message;
    @BeforeMethod
    public void setUp() throws Exception {
        byte[] uint32_1 = {(byte)0xff, (byte)0x5a, (byte)0x86, (byte)0xc0};
        message = new KeepAliveC(uint32_1);
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testKeepAliveC() throws Exception {
        long clientTime = 0xFF535726L;
        message = new KeepAliveC(clientTime);
        Assert.assertEquals(message.getClientTime(), clientTime);
    }

    @Test
    public void testGetClientTime() throws Exception {
        Assert.assertEquals(message.getClientTime(), 4284122816L);
    }

}