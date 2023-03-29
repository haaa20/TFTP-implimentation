package org.example;

import myTftp.FileManager;
import myTftp.TftpClient;
import myTftp.TftpUser;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientThread extends Thread {
    private FileManager fileManager;
    private TftpClient client;

    public ClientThread() {
        fileManager = new FileManager("clientStorage");
        client = new TftpClient("Client", 9001);
    }

    @Override
    public void run() {
        client.say("Online...");

        fileManager.open("someText.txt");
        String message = fileManager.readFull();

        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // DO STUFF BELOW
        client.setDebug(true);
        client.sendData(serverAddress, 9000, message.getBytes());
    }
}

