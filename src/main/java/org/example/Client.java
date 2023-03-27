package org.example;

import myTftp.TftpUser;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client extends TftpUser implements Runnable{
    public Client(int portNo) throws SocketException {
        super("Client", portNo);
    }

    @Override
    public void run() {

    }
}
