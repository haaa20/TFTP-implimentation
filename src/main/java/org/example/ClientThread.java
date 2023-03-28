package org.example;

import myTftp.FileManager;
import myTftp.TftpUser;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientThread extends Thread {
    private FileManager fileManager;

    public ClientThread() {
        fileManager = new FileManager("files");
    }

    @Override
    public void run() {
        System.out.println("Hello! I am the client");

        TftpUser client = new TftpUser("Client", 9001);
        String message = "Hello mr Server, how's the wife and kids?";
        InetAddress serverAddress;

        try {
            serverAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // DO STUFF BELOW
        client.sendData(serverAddress, 9000, message.getBytes());
    }
}

