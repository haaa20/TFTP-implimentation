package org.example;

import myTftp.TftpUser;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientThread extends Thread {
    @Override
    public void run() {
        System.out.println("Hello! I am the client");
        TftpUser client = new TftpUser("Client", 9001);
        InetAddress serverAddress;

        try {
            serverAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // DO STUFF BELOW
        int i = client.sendSingleData(serverAddress, 9000, "HELLO".getBytes(), 1);
        System.out.println(i);
    }
}

