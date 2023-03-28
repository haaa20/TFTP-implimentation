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
        fileManager.open("tea.txt");

        TftpUser client = new TftpUser("Client", 9001);
        String message = fileManager.readFull();
        InetAddress serverAddress;

        try {
            serverAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // DO STUFF BELOW
        int i = client.sendSingleData(serverAddress, 9000, message.getBytes(), 1);
        System.out.println(i);
    }
}

