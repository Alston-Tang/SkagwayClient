package me.thm64.utility;

import me.thm64.worker.socket.InetSocket;
import org.testng.annotations.Test;

import java.net.InetAddress;

import static org.testng.Assert.*;

public class FragRecorderTest {

    @Test
    public void testRecord() throws Exception {
        InetSocket socket = new InetSocket(InetAddress.getByName("192.168.123.1"), 12345);
        FragRecorder recorder = new FragRecorder(socket);

        recorder.record(10);

        recorder.analysis();
    }
}