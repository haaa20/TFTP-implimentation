package org.example;

import myTftp.FileManager;
import myTftp.TftpClient;
import myTftp.TftpUser;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientThread extends Thread {
    private TftpClient client;

    public ClientThread() {
        client = new TftpClient("Client", 9001);
        client.setFileHomePath("clientStorage");
    }

    @Override
    public void run() {
        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // DO STUFF BELOW
        String message = "Nothing to say";

        client.sendFile(serverAddress, 9000, "someText.txt");
    }
}

