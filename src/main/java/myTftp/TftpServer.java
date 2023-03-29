package myTftp;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.HashMap;

public class TftpServer extends TftpUser implements Runnable {
    private boolean running;
    private HashMap<Integer, SocketAddress> clients;
    private int nextUniqueId;

    public TftpServer(String name, int portNo) {
        super(name, portNo);
        this.running = false;
        this.nextUniqueId = 0;
        this.clients = new HashMap<>();
    }

    private void awaitRequests() {
        while (running) {
            DatagramPacket request = rawReceive();
            clients.put(nextUniqueId++, request.getSocketAddress());
        }
    }

    @Override
    public void run() {
        running = true;
        awaitRequests();
    }

    public void terminate() {
        running = false;
    }
}
