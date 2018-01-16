package me.thm64.protocol;

import me.thm64.utility.Util;

public class DataSegment extends SkagwayMessage {
    public final static int FRAG_MAX_SIZE = 1400;

    public DataSegment(byte[] data, int length) {
        this.data = data;
        this.length = length;
    }

    public DataSegment(long time, long frameSeq, int totalFrag, int curFrag, byte[] imgFrag) {
        this.length = SkagwayMessage.DATA_SEGMENT_HEADER_LENGTH + imgFrag.length;
        this.data = new byte[this.length];
        Util.longToUint32(time, this.data, 0);
        Util.longToUint32(frameSeq, this.data, 4);
        Util.intToUint16(totalFrag, this.data, 8);
        Util.intToUint16(curFrag, this.data, 10);
        System.arraycopy(imgFrag, 0, this.data, 12, imgFrag.length);
    }

    public long getTime() {
        return Util.uint32ToLong(this.data, 0);
    }

    public long getFrameSeq() {
        return Util.uint32ToLong(this.data, 4);
    }

    public int getTotalFrag() {
        return Util.uint16ToInt(this.data, 8);
    }

    public int getCurFrag() {
        return Util.uint16ToInt(this.data, 10);
    }

    public int getDataLen() {
        return this.length - SkagwayMessage.DATA_SEGMENT_HEADER_LENGTH;
    }

    public void copyImg(byte[] buffer, int offset, int len) {
        System.arraycopy(this.data, SkagwayMessage.DATA_SEGMENT_HEADER_LENGTH, buffer, offset, this.getDataLen());
    }
}
