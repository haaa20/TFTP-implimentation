package org.example;

import myTftp.FileManager;
import myTftp.TftpServer;
import myTftp.TftpUser;

import java.util.ArrayList;

public class ServerThread extends Thread {
    private FileManager fileManager;
    private TftpServer server;

    public ServerThread() {
        fileManager = new FileManager("clientStorage");
        server = new TftpServer("Server", 9000);
    }
    @Override
    public void run() {
        server.say("Online...");

        // DO STUFF BELOW
        ArrayList<byte[]> buf = new ArrayList<>();
        String message;

        server.receiveData(buf);
        message = new String(server.assembleData(buf));

        System.out.println(message);
    }
}