package myTftp;

import java.net.DatagramPacket;
import java.net.SocketAddress;
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
            int opcode = TftpPacket.extractOpcode(request);

            if (opcode == 1) {
                // READ REQUEST
            }
            else if (opcode == 2) {
                // WRITE REQUEST

                // Acknowledge the request and wait for the client to begin sending data
                acknowledge(request);
                byte[] writeBuf = receiveAndAssemble();

                // Save the data we have received
            }
            else {
                sendError(request, "Not a read or write request");
            }
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

    // What has each client actually requested?
    private class ClientRequest {
        private String fileName;
        private WRMode mode;
        private SocketAddress clientAddress;

        public ClientRequest(String fileName, WRMode mode, DatagramPacket requestPacket) {
            this.fileName = fileName;
            this.mode = mode;
            this.clientAddress = requestPacket.getSocketAddress();
        }

        public String getFileName() {
            return fileName;
        }

        public WRMode getMode() {
            return mode;
        }

        public SocketAddress getClientAddress() {
            return clientAddress;
        }
    }
}
