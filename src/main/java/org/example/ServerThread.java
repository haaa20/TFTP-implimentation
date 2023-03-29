package org.example;

import myTftp.FileManager;
import myTftp.TftpUser;

import java.util.ArrayList;

public class ServerThread extends Thread {
    private FileManager fileManager;
    private TftpUser server;

    public ServerThread() {
        fileManager = new FileManager("clientStorage");
        server = new TftpUser("Server", 9000);
    }
    @Override
    public void run() {
        server.say("Online...");

        // DO STUFF BELOW
        ArrayList<byte[]> buf = new ArrayList<>();
        server.setDebug(true);
        server.receiveData(buf);
    }
}