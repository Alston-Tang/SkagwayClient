package me.thm64.worker.socket;

import me.thm64.protocol.SkagwayMessage;

import java.util.concurrent.TimeUnit;

public interface SocketInterface {
    SkagwayMessage receive();
    SkagwayMessage receive(int timeout); //ms;
    void send(SkagwayMessage message);
}