package org.example;

import myTftp.FileManager;
import myTftp.TftpServer;
import myTftp.TftpUser;

import java.util.ArrayList;

public class ServerThread extends Thread {
    private TftpServer server;

    public ServerThread() {
        server = new TftpServer("Server", 9000);
    }
    @Override
    public void run() {
        server.setDebug(true);
        server.say("Online...");

        // DO STUFF BELOW
        server.run();
    }
}