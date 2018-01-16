package me.thm64.protocol;

import me.thm64.utility.Util;

public abstract class SkagwayMessage {
    public final static int DATA_SEGMENT_HEADER_LENGTH = 12;
    public final static int KEEP_ALIVE_S_HEADER_LENGTH = 8;
    public final static int KEEP_ALIVE_C_HEADER_LENGTH = 4;

    byte[] data;
    int length;

    /* Return REFERENCE of data */
    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return this.length;
    }

    public boolean equals(SkagwayMessage message) {
        for (int i = 0; i < this.length; i++) {
            if (this.data[i] != message.getData()[i]) return false;
        }
        return true;
    }

}
