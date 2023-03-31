package org.example;
public class Run {
    public static void main(String[] args) {
        ClientThread clientThread = new ClientThread();
        ServerThread serverThread = new ServerThread();

        clientThread.start();
        // serverThread.start();
    }
}
