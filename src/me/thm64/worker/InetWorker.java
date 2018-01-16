package me.thm64.worker;

import me.thm64.worker.socket.InetSocket;
import me.thm64.utility.SkagwayImage;
import me.thm64.worker.socket.SocketInterface;

import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;

public class InetWorker extends SkagwayWorker{

    private final InetSocket socket;

    public InetWorker(int sourceId, ArrayBlockingQueue<SkagwayImage> outQueue, InetAddress address, int port) {
        super(sourceId, outQueue);
        this.socket = new InetSocket(address, port);
    }


    @Override
    SocketInterface getSocket() {
        return this.socket;
    }
}
