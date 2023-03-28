package org.example;

import myTftp.TftpUser;

public class ServerThread extends Thread {
    @Override
    public void run() {
        System.out.println("Hello! I am the server");
        TftpUser server = new TftpUser("Server", 9000);

        // DO STUFF BELOW
        byte[] data = server.receiveSingleData();
        System.out.println(new String(data));
    }
}