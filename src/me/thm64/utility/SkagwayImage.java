package me.thm64.utility;

public class SkagwayImage {
    public static enum imageType {jpeg};

    public imageType type = imageType.jpeg;
    public byte[] data;
    public int size;
    public long time;
    public long frameSeq;
    public int sourceId;

    public SkagwayImage(int maxSize, long time, long frameSeq) {
        this.size = 0;
        this.data = new byte[maxSize];
        this.time = time;
        this.frameSeq = frameSeq;
        this.sourceId = -1;
    }
}
