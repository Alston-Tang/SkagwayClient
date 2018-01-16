package me.thm64.worker.socket;

import me.thm64.mock.MockServer;
import me.thm64.protocol.DataSegment;
import me.thm64.protocol.KeepAliveC;
import me.thm64.protocol.SkagwayMessage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetAddress;

public class InetSocketTest {
    @BeforeMethod
    public void setUp() throws Exception {
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testReceive() throws Exception {
        InetSocket socket = new InetSocket(InetAddress.getByName("127.0.0.1"), 12344);
        long startTime = System.currentTimeMillis();
        socket.receive(1200);
        long stopTime = System.currentTimeMillis();
        Assert.assertTrue(stopTime - startTime > 1200);
        Assert.assertTrue(stopTime - startTime < 1500);
    }

    @Test
    public void testReceive1() throws Exception {
        MockServer mockServer = new MockServer(12344);
        MockServer wrongServer = new MockServer(12343);

        InetSocket socket = new InetSocket(InetAddress.getByName("127.0.0.1"), 12344);

        int clientPort = socket.getPort();
        InetAddress clientAddress = InetAddress.getByName("127.0.0.1");

        byte[] wrongData = {'a', 'b', 'c'};
        KeepAliveC correctData = new KeepAliveC(8888L);

        Thread receiver = new Thread(() -> {
            SkagwayMessage message = socket.receive();

            Assert.assertEquals(message.getLength(), SkagwayMessage.KEEP_ALIVE_C_HEADER_LENGTH);

            KeepAliveC receivedMessage = (KeepAliveC)message;
            Assert.assertEquals(receivedMessage.equals(correctData), true);

        });

        receiver.start();


        mockServer.send(wrongData, clientAddress, clientPort);
        wrongServer.send(correctData.getData(), clientAddress, clientPort);
        mockServer.send(correctData.getData(), clientAddress, clientPort);

        receiver.join();
    }

    @Test
    public void testSend() throws Exception {
        MockServer mockServer = new MockServer(12345);
        Thread serverThread = new Thread(mockServer);
        serverThread.start();

        Thread.sleep(100);

        InetSocket socket = new InetSocket(InetAddress.getByName("127.0.0.1"), 12345);
        KeepAliveC message = new KeepAliveC(1234L);

        socket.send(message);

        mockServer.stopServer();
        serverThread.join();

        Assert.assertEquals(mockServer.receiveList.size(), 1);

        MockServer.MessageRecord record = mockServer.receiveList.get(0);
        byte[] data = record.data;

        Assert.assertNotEquals(data.length, message.getLength());
        KeepAliveC receivedMessage = new KeepAliveC(data);

        Assert.assertEquals(receivedMessage.equals(message), true);
    }

    @Test
    public void testSendDataSegment() throws Exception {
        MockServer mockServer = new MockServer(12346);
        Thread serverThread = new Thread(mockServer);
        serverThread.start();

        Thread.sleep(100);

        InetSocket socket = new InetSocket(InetAddress.getByName("127.0.0.1"), 12346);
        byte[] img = {'a','b','c','d'};
        DataSegment message = new DataSegment(1L, 2L, 3, 4, img);

        socket.send(message);

        mockServer.stopServer();
        serverThread.join();

        Assert.assertEquals(mockServer.receiveList.size(), 1);

        MockServer.MessageRecord record = mockServer.receiveList.get(0);
        byte[] data = record.data;
        Assert.assertNotEquals(data.length, message.getLength());

        DataSegment receivedMessage = new DataSegment(data, message.getLength());
        Assert.assertEquals(receivedMessage.equals(message), true);
    }



}