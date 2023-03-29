package myTftp;

import java.net.DatagramPacket;
import java.util.ArrayDeque;
import java.util.Queue;

public class TftpServer extends TftpUser implements Runnable {
    private boolean running;
    private Queue<ClientRequest> clientRequests;

    public TftpServer(String name, int portNo) {
        super(name, portNo);
        this.running = false;
        this.clientRequests = new ArrayDeque<>();
    }

    private void awaitRequests() {
        while (running) {
            DatagramPacket request = rawReceive();
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

    // Do they want to read or write?
    private enum Mode {READ, WRITE}

    // What has each client actually requested?
    private class ClientRequest {
        private String fileName;
        private Mode mode;

        public ClientRequest(String fileName, Mode mode) {
            this.fileName = fileName;
            this.mode = mode;
        }

        public String getFileName() {
            return fileName;
        }

        public Mode getMode() {
            return mode;
        }
    }
}
