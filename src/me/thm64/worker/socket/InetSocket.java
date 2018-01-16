package me.thm64.worker.socket;

import me.thm64.protocol.*;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class InetSocket implements SocketInterface {

    private static final int RECEIVE_BUFFER_LEN = 1600;
    private static final int SOCKET_TIMEOUT = 200;

    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    public InetSocket(InetAddress address, int port) {
        try {
            this.socket = new DatagramSocket();
            this.socket.setSoTimeout(SOCKET_TIMEOUT);
        }
        catch (SocketException e) {
            // TODO Handle error
            System.err.println("Error: InetSocket: InetSocket Exception");
        }
        this.socket.connect(address, port);
        if (!this.socket.isConnected()) {
            // TODO Handle error
            System.err.println("Error: InetSocket: Can not connect to server");
        }
        this.address = address;
        this.port = port;
    }


    public SkagwayMessage receive() {
        return this.receive(-1);
    }

    public SkagwayMessage receive(int timeout) {
        SkagwayMessage rv = null;
        byte[] buffer = new byte[RECEIVE_BUFFER_LEN];
        DatagramPacket packet = new DatagramPacket(buffer, RECEIVE_BUFFER_LEN);
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        while (timeout < 0 || startTime + timeout > currentTime) {
            try {
                socket.receive(packet);
            }
            catch (SocketTimeoutException e) {
                currentTime = System.currentTimeMillis();
                continue;
            }
            catch (IOException e) {
                //TODO Handle error
                System.err.println("Error: receive: IOException");
                System.exit(-1);
            }

            if (packet.getPort() != port || !packet.getAddress().equals(address))
                continue;

            int packetLen = packet.getLength();
            switch (packetLen) {
                case SkagwayMessage.KEEP_ALIVE_C_HEADER_LENGTH:
                    rv = new KeepAliveC(packet.getData());
                    break;
                case SkagwayMessage.KEEP_ALIVE_S_HEADER_LENGTH:
                    rv = new KeepAliveS(packet.getData());
                    break;
                default:
                    if (packetLen >= SkagwayMessage.DATA_SEGMENT_HEADER_LENGTH) {
                        rv = new DataSegment(packet.getData(), packetLen);
                    }
                    break;
            }

            if (rv != null) break;
        }

        return rv;
    }

    public void send(SkagwayMessage message) {
        DatagramPacket packet = new DatagramPacket(message.getData(), message.getLength());
        try {
            socket.send(packet);
        }
        catch (IOException e) {
            //TODO Handle Error
            System.err.println("Error: send: IOException");
            System.exit(-1);
        }
    }

    public int getPort() {
        return this.socket.getLocalPort();
    }
}
