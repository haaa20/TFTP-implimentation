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
        // Ingredients for one homemade ClientRequest...
        DatagramPacket request;
        int opcode;
        String pathname;
        WRMode wr;

        while (running) {
            request = rawReceive();
            opcode = TftpPacket.extractOpcode(request);

            if (opcode == 1) {
                // READ REQUEST
                pathname = TftpPacket.extractPathname(request);
                wr = WRMode.READ;
                acknowledge(request);
            }
            else if (opcode == 2) {
                // WRITE REQUEST
                pathname = TftpPacket.extractPathname(request);
                wr = WRMode.WRITE;
                acknowledge(request);
            }
            else {
                sendError(request, "Not a read or write request");
                continue;
            }

            clientRequests.add(new ClientRequest(pathname, wr, request));
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
