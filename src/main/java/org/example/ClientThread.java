package org.example;

import myTftp.FileManager;
import myTftp.TftpClient;
import myTftp.TftpUser;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientThread extends Thread {
    private TftpClient client;

    public ClientThread() {
        client = new TftpClient("Client", 9002);
        client.setFileHomePath("clientStorage");
    }

    @Override
    public void run() {
        client.setDebug(true);

        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // DO STUFF BELOW
        String message = "Nothing to say";

        client.requestWrite(serverAddress, 9000, "lyrics.xml");

        client.requestRead(serverAddress, 9000, "hotFood.txt");
    }
}

