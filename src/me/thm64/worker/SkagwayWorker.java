package me.thm64.worker;

import me.thm64.worker.assembler.Assembler;
import me.thm64.protocol.DataSegment;
import me.thm64.protocol.KeepAliveC;
import me.thm64.protocol.KeepAliveS;
import me.thm64.protocol.SkagwayMessage;
import me.thm64.worker.socket.SocketInterface;
import me.thm64.utility.SkagwayImage;
import me.thm64.utility.Util;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class SkagwayWorker implements Runnable{
    //TODO TimeOffset Filter
    private static final int CLIENT_EXPIRE_TIME = 10 * 1000; //ms
    private static final int SOCKET_TIMEOUT = 500; //ms

    public static class TimeOffset {
        public long val;
        public boolean valid;
        TimeOffset() {
            this.valid = false;
        }
    }

    //Ownership => SkagwayWorkerThread
    final private int sourceId;
    final private Assembler assembler;
    final TimeOffset timeOffset;

    //Ownership => shared by Worker and Controller
    final private ArrayBlockingQueue<SkagwayImage> outQueue;

    //Ownership => Controller
    public boolean stop;

    private class KeepAliveRunnable implements Runnable{
        //Ownership => SkagwayWorkerThread;
        public boolean stop;
        SocketInterface socket;

        KeepAliveRunnable(SocketInterface socket) {
           this.socket = socket;
        }

        public void run() {
            long lastSentTime = 0;
            while (!stop) {
                long currentTime = Util.curTimeUint32();
                if (currentTime - lastSentTime > SkagwayWorker.CLIENT_EXPIRE_TIME * 2 / 3) {
                    KeepAliveC message = new KeepAliveC(currentTime);
                    this.socket.send(message);
                    lastSentTime = currentTime;
                }
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    //TODO What is happening?
                    System.err.println("Warn: KeepAliveThread: Receive interrupt");
                }
            }
        }
    }

    SkagwayWorker(int sourceId, ArrayBlockingQueue<SkagwayImage> outQueue) {
        this.sourceId = sourceId;
        this.assembler = new Assembler();
        this.stop = true;
        this.outQueue = outQueue;
        this.timeOffset = new TimeOffset();
    }

    public void run() {
        SocketInterface socket = this.getSocket();
        KeepAliveRunnable keepAliveRunnable = new KeepAliveRunnable(socket);
        keepAliveRunnable.stop = false;
        Thread keepAliveThread = new Thread(keepAliveRunnable);


        keepAliveThread.start();
        while (!this.stop) {
            SkagwayMessage message = socket.receive(SOCKET_TIMEOUT);
            if (message == null) continue;
            if (message.getLength() == SkagwayMessage.KEEP_ALIVE_S_HEADER_LENGTH) {
                KeepAliveS keepAliveS = (KeepAliveS)message;
                this.timeOffset.valid = true;
                this.timeOffset.val = keepAliveS.getTimeOffset(Util.curTimeUint32());
            }
            else if(message.getLength() >= SkagwayMessage.DATA_SEGMENT_HEADER_LENGTH) {
                if (!this.timeOffset.valid)
                    continue;

                DataSegment dataSegment = (DataSegment)message;
                this.assembler.feed(dataSegment);
                if (this.assembler.available) {
                    SkagwayImage image = this.assembler.get();
                    image.time += this.timeOffset.val;
                    image.sourceId = this.sourceId;
                    this.outQueue.offer(image);
                }
            }
        }

        keepAliveRunnable.stop = true;
        while (true) {
            try {
                keepAliveThread.join();
            }
            catch (InterruptedException e) {
                //TODO What is happening?
                System.err.println("Warn: SkagwayThread: Receive interrupt");
                continue;
            }
            break;
        }
    }

    public int getId() {
        return this.sourceId;
    }

    public TimeOffset getTimeOffset() {
        return this.timeOffset;
    }

    abstract SocketInterface getSocket();
}
