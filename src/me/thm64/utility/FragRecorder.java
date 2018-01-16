package me.thm64.utility;

import me.thm64.protocol.DataSegment;
import me.thm64.protocol.KeepAliveC;
import me.thm64.protocol.SkagwayMessage;
import me.thm64.worker.socket.SocketInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class FragRecorder {

    private class FragObj {
        TreeMap<Integer, TimeObj> frags;
        int totalFrag;
    }

    private class TimeObj {
        long sentTime;
        long receivedTime;
    }
    private SocketInterface socket;
    private TreeMap<Long, FragObj> record;

    FragRecorder(SocketInterface socket) {
        this.record = new TreeMap<Long, FragObj>();
        this.socket = socket;
    }

    void record(int second) {
        long currentTime = System.currentTimeMillis();
        long endTime = currentTime + second * 1000;


        long lastAliveSent = System.currentTimeMillis();
        KeepAliveC keepAliveC = new KeepAliveC(lastAliveSent);
        this.socket.send(keepAliveC);

        while (currentTime < endTime) {
            SkagwayMessage message = this.socket.receive((int)(endTime - currentTime));

            currentTime = System.currentTimeMillis();
            if (currentTime - lastAliveSent > 5000) {
                lastAliveSent = System.currentTimeMillis();
                keepAliveC = new KeepAliveC(lastAliveSent);
                this.socket.send(keepAliveC);
            }
            if (message == null) continue;
            if (message.getLength() < SkagwayMessage.DATA_SEGMENT_HEADER_LENGTH) continue;

            DataSegment frag = (DataSegment)message;

            long frameSeq = frag.getFrameSeq();
            int curFrag = frag.getCurFrag();

            FragObj fragObj = record.get(frameSeq);
            if (fragObj == null) {
                fragObj = new FragObj();
                fragObj.totalFrag = frag.getTotalFrag();
                fragObj.frags = new TreeMap<Integer, TimeObj>();
                record.put(frameSeq, fragObj);
            }

            TimeObj timeObj = fragObj.frags.get(curFrag);
            if (timeObj != null) {
                System.err.println("Duplicate fragment @ frame " + frameSeq + " frag " + curFrag);
                continue;
            }

            timeObj = new TimeObj();
            timeObj.receivedTime = currentTime;
            timeObj.sentTime = frag.getTime();
            fragObj.frags.put(curFrag, timeObj);
        }
    }

    void analysis() {
        System.out.println("Receive " + this.record.size() + " frames");

        for(Map.Entry<Long, FragObj> entry : this.record.entrySet()) {
            long frameSeq = entry.getKey();
            FragObj fragObj = entry.getValue();

            System.out.println("Frame " + frameSeq + " has " + fragObj.totalFrag + " fragments while receive " + fragObj.frags.size() + " fragments");
            long maxDelay = 0;
            for (Map.Entry<Integer, TimeObj> e : fragObj.frags.entrySet()) {
                maxDelay = Math.max(e.getValue().receivedTime - e.getValue().sentTime, maxDelay);
            }

            System.out.println("Max delay: " + maxDelay);
        }
    }
}
