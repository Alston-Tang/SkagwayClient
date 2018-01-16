package me.thm64.mock;

import me.thm64.protocol.SkagwayMessage;

import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;

public class MockServer implements Runnable {
    static public class MessageRecord {
        public long time;
        public byte[] data;
    }


    private DatagramSocket socket;
    public ArrayList<MessageRecord> receiveList, sendList;
    private volatile boolean stop;

    public MockServer(int port) throws Exception{
        socket = new DatagramSocket(port);
        socket.setSoTimeout(500);
        receiveList = new ArrayList<MessageRecord>();
        sendList = new ArrayList<>();
        stop = false;
    }

    public void stopServer() {
        if (stop) return;
        stop = true;
    }

    public void send(byte[] data, InetAddress address, int port) throws Exception{
        DatagramPacket packet = new DatagramPacket(data, data.length);
        packet.setAddress(address);
        packet.setPort(port);

        this.socket.send(packet);
        MessageRecord record = new MessageRecord();
        record.data = data;
        record.time = new Date().getTime();
        this.sendList.add(record);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1500];
        DatagramPacket packet = new DatagramPacket(buffer, 1500);
        while (!this.stop) {
            try {
                socket.receive(packet);
            }
            catch (SocketTimeoutException e) {
                continue;
            }
            catch (IOException e) {
                System.err.println("run: IOException");
            }

            MessageRecord record = new MessageRecord();
            record.data = packet.getData();
            record.time = new Date().getTime();
            receiveList.add(record);
        }
    }
}
