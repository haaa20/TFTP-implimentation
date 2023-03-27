package org.example;

import myTftp.TftpUser;

import java.net.SocketException;

public class Server extends TftpUser implements Runnable{
    public Server(int portNo) throws SocketException {
        super("server", portNo);
    }

    @Override
    public void run() {
        receiveData();
    }
}
