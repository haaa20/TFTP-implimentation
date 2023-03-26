package org.example;

import java.net.SocketException;

public class Run {
    public static void main(String[] args) throws SocketException {
        Runnable thing = new Client(9000);
        thing.run();
    }
}
