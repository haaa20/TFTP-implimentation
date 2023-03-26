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
        try {
            InetAddress serverAddress = InetAddress.getByName("localhost");
            send(serverAddress, 9001);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }
}
