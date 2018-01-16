package me.thm64.protocol;

import me.thm64.utility.Util;

public class KeepAliveS extends SkagwayMessage {

    public KeepAliveS(byte[] data) {
        this.length = SkagwayMessage.KEEP_ALIVE_S_HEADER_LENGTH;
        this.data = data;
    }

    public KeepAliveS(long clientTime, long serverTime) {
        this.length = SkagwayMessage.KEEP_ALIVE_S_HEADER_LENGTH;
        this.data = new byte[this.length];
        Util.longToUint32(clientTime, this.data, 0);
        Util.longToUint32(serverTime, this.data, 4);
    }

    public long getClientTime() {
        return Util.uint32ToLong(this.data, 0);
    }

    public long getServerTime() {
        return Util.uint32ToLong(this.data, 4);
    }

    public long getTimeOffset(long curTimeUint32) {
        if (curTimeUint32 > 0xffffffffL) {
            //TODO Handle Error
            System.err.println("Error: getTimeOffset: Invalid input current time");
            return -1;
        }
        return curTimeUint32 - (this.getServerTime() + (curTimeUint32 - this.getClientTime()) / 2);
    }
}
