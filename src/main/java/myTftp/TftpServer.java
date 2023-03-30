package myTftp;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

import static java.lang.Thread.sleep;

public class TftpServer extends TftpUser implements Runnable {
    private boolean running;

    public TftpServer(String name, int portNo) {
        super(name, portNo);
        this.running = false;
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            DatagramPacket p = rawReceive();
            int op = TftpPacket.extractOpcode(p);

            if (op == 1 || op == 2) {
                // A new request
            }
            else if (op == 3) {
                // A new data packet
            }
        }
    }

    private void handleWriteRequest() {
        byte[] data = receiveAndAssemble();
        System.out.println(new String(data));
    }

    private void handleReadRequest() {

    }

    public void terminate() {
        running = false;
    }

    private class ClientRequest {
        private String fileName;
        private WRMode wr;
        private SocketAddress clientAddress;

        public ClientRequest(String fileName, WRMode mode, DatagramPacket requestPacket) {
            this.fileName = fileName;
            this.wr = mode;
            this.clientAddress = requestPacket.getSocketAddress();
        }
    }
}
