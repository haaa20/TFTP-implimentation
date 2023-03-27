package org.example;

import myTftp.TftpPacket;
import myTftp.TftpUser;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Run {

    private static Thread serverThread = new Thread() {
        @Override
        public void run() {
            System.out.println("Hello! I am the server");
            TftpUser server = new TftpUser("Server", 9000);

            // DO STUFF BELOW
            byte[] data = server.receiveSingleData();
            System.out.println(new String(data));
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
            int i = client.sendSingleData(serverAddress, 9000, "HELLO".getBytes(), 1);
            System.out.println(i);
        }
    };

    public static void main(String[] args) throws SocketException {
        clientThread.start();
        serverThread.start();
    }
}
