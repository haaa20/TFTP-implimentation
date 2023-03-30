package myTftp;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

import static java.lang.Thread.sleep;

public class TftpServer extends TftpUser implements Runnable {
    private boolean running;
    private Queue<ClientRequest> clientRequests;
    private RequestHandler requestHandler;
    private ClientRequest currentRequest;

    public TftpServer(String name, int portNo) {
        super(name, portNo);
        this.running = false;
        this.clientRequests = new ArrayDeque<>();
        this.requestHandler = new RequestHandler();
    }

    @Override
    public void run() {
        running = true;
        requestHandler.start();

        while (running) {
            // If there's nothing in the queue, wait a short time, and check again
            if (clientRequests.isEmpty()) {
                try {
                    sleep(250);
                    continue;
                } catch (InterruptedException e) {
                    continue;
                }
            }
            handleTopRequest();
        }
    }

    private void handleWriteRequest() {
        byte[] data = receiveAndAssemble();
        System.out.println(new String(data));
    }

    private void handleReadRequest() {

    }

    private void handleTopRequest() {
        currentRequest = clientRequests.poll();

        switch (currentRequest.wr) {
            case READ:
                handleReadRequest();
            case WRITE:
                handleWriteRequest();
        }

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

    public void terminate() {
        running = false;
    }

    // My God, I am making my life complicated
    private class RequestHandler extends Thread {
        @Override
        public void run() {
            awaitRequests();
        }
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
