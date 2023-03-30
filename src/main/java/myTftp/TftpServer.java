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

            if (op == 1) {
                // A new READ request
            }
            else if (op == 2) {
                // A new WRITE request
            }
            else if (op == 3) {
                // A new DATA packet
            }
            else if (op == 4) {
                // A new ACK packet!
                // ... Which, remember, will be in response to an outgoing data packet!
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

}
