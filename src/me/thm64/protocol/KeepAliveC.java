package me.thm64.protocol;

import me.thm64.utility.Util;

public class KeepAliveC extends SkagwayMessage {

    public KeepAliveC(byte[] packet) {
        this.data = packet;
        this.length = SkagwayMessage.KEEP_ALIVE_C_HEADER_LENGTH;
    }

    public KeepAliveC(long clientTime) {
        this.length = SkagwayMessage.KEEP_ALIVE_C_HEADER_LENGTH;
        this.data = new byte[this.length];
        Util.longToUint32(clientTime, this.data, 0);
    }

    public long getClientTime() {
        return Util.uint32ToLong(this.data, 0);
    }
}
