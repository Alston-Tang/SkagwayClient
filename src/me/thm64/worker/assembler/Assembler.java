package me.thm64.worker.assembler;

import me.thm64.protocol.DataSegment;
import me.thm64.utility.SkagwayImage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

public class Assembler {

    class AssemblerNode {
        SkagwayImage image;
        int remainFrag;
        int totalFrag;
        long frameSeq;
        boolean[] fed;

        AssemblerNode(int totalFrag, long frameSeq, SkagwayImage image) {
            this.totalFrag = totalFrag;
            this.remainFrag = totalFrag;
            this.frameSeq = frameSeq;
            this.image = image;
            fed = new boolean[totalFrag];
            Arrays.fill(fed, false);
        }
    }

    private LinkedList<AssemblerNode> list;
    private long minFrameSeq;
    public boolean available;

    public Assembler() {
        this.list = new LinkedList<AssemblerNode>();
        this.minFrameSeq = -1;
        this.available = false;
    }

    public boolean feed(DataSegment message) {
        if (message.getFrameSeq() < this.minFrameSeq)
            return false;

        ListIterator<AssemblerNode> it = this.list.listIterator();
        AssemblerNode frameNode = null;
        while (it.hasNext()) {
            AssemblerNode curNode = it.next();
            if (curNode.frameSeq > message.getFrameSeq()) {
                it.previous();
                break;
            }
            else if (curNode.frameSeq == message.getFrameSeq()) {
                frameNode = curNode;
                break;
            }
        }

        if (frameNode == null) {
            int maxSize = message.getTotalFrag() * DataSegment.FRAG_MAX_SIZE;

            SkagwayImage image = new SkagwayImage(maxSize, message.getTime(), message.getFrameSeq());
            AssemblerNode newNode = new AssemblerNode(message.getTotalFrag(), message.getFrameSeq(), image);

            it.add(newNode);

            frameNode = it.previous();
        }

        if (frameNode.fed[message.getCurFrag()])
            return false;

        int offset = message.getCurFrag() * DataSegment.FRAG_MAX_SIZE;
        message.copyImg(frameNode.image.data, offset, message.getDataLen());
        frameNode.image.size += message.getDataLen();
        frameNode.remainFrag -= 1;
        if (frameNode.remainFrag == 0) {
            this.available = true;
            this.minFrameSeq = Math.max(this.minFrameSeq, frameNode.frameSeq);
        }
        frameNode.fed[message.getCurFrag()] = true;


        return true;
    }

    public SkagwayImage get() {
        SkagwayImage rv = null;

        if (!this.available)
            return null;
        this.available = false;

        ListIterator<AssemblerNode> it = this.list.listIterator();

        long lastAvailableFrameSeq = -1;

        while (it.hasNext()) {
            AssemblerNode cur = it.next();

            if (cur.remainFrag == 0) {
                lastAvailableFrameSeq = cur.frameSeq;
            }
        }

        it = this.list.listIterator();

        while (it.hasNext()) {
            AssemblerNode cur = it.next();
            if (cur.frameSeq < lastAvailableFrameSeq) {
                it.remove();
            } else if (cur.frameSeq == lastAvailableFrameSeq) {
                rv = cur.image;
                it.remove();
                break;
            }
        }

        return rv;
    }
}
