package org.example;

import myTftp.TftpPacket;
import myTftp.TftpUser;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Run {

    private static Thread serverThread = new Thread() {
        @Override
        public void run() {
            System.out.println("Hello! I am the server");
            TftpUser server = new TftpUser("Server", 9000);

            // DO STUFF BELOW
            server.receiveData();
        }
    };

    private static Thread clientThread = new Thread() {
        @Override
        public void run() {

            System.out.println("Hello! I am the client");
            TftpUser client = new TftpUser("Client", 9001);
            InetAddress serverAddress;

            try {
                serverAddress = InetAddress.getByName("localhost");
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

            // DO STUFF BELOW
            client.sendSingleData(serverAddress, 9000, "HELLO".getBytes(), 1);
        }
    };

    public static void main(String[] args) throws SocketException {
        clientThread.start();
        serverThread.start();
    }
}
