package myTftp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.*;

import static java.lang.Thread.sleep;

public class TftpServer extends TftpUser implements Runnable {
    private boolean running;
    private Map<SocketAddress, ReadHandler> readConnections;
    private Map<SocketAddress, WriteHandler> writeConnections;

    public TftpServer(String name, int portNo) {
        super(name, portNo);
        this.running = false;
        this.readConnections = new HashMap<>();
        this.writeConnections = new HashMap<>();
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            DatagramPacket p = rawReceive();
            int op = TftpPacket.extractOpcode(p);

            String s = new String(p.getData());

            if (op == 1) {
                // A new READ request
                // Map the request to a new string and send the first block
                // TODO
            }
            else if (op == 2) {
                // A new WRITE request
                // Map the request to a new handler thread, and acknowledge (which is done from INSIDE the new thread)
                WriteHandler handler = new WriteHandler(p);
                handler.start();
            }
        }
    }

    /**
     * Handles an incoming data packet form a client
     *
     * @param p Packet
     * @param buffer List
     * @return True if the last packet
     */
    private boolean handleDataPacket(DatagramPacket p, List<byte[]> buffer) {
        // Assume that the socket has done its job and a buffer list is in place
        byte[] data = TftpPacket.extractData(p.getData(), p.getLength());
        int length = data.length;
        buffer.add(data);
        acknowledge(p);

        return  (length < TFTP_CAPACITY - 4);
    }

//    private boolean sendIthDataPacket(SocketAddress address, int i) {
//        byte[] data = dataWindow(readConnections.get(address), i);
//        byte[] wrappedData = new DataTftpPacket(i, data).toBytes();
//        DatagramPacket p = new DatagramPacket(wrappedData, wrappedData.length);
//
//        return rawSend(p);
//    }

    // Send and acknowledgment from the corresponding handler port
    @Override
    protected void acknowledge(DatagramPacket p) {
        SocketAddress address = p.getSocketAddress();
        if (!writeConnections.containsKey(address)) {
            super.acknowledge(p);
        }
        DatagramSocket s = writeConnections.get(address).tempSocket;

        // Preparing the ack packet
        DatagramPacket ackPacket = newAck(p);

        // Sending the ack packet
        rawSend(ackPacket, s);
    }

    /**
     * One of these is launched for each unique client-rrq, and is responsible for
     * handling the request until completion
     */
    private class ReadHandler extends Thread {
        @Override
        public void run() {
            super.run();
        }
    }

    /**
     * One of these is launched for each unique client-wrq, and is responsible for
     * handling the request until completion
     */
    private class WriteHandler extends Thread {
        DatagramSocket tempSocket;
        SocketAddress clientAddress;
        List<byte[]> dataBuffer;
        String pathname;
        boolean running;

        public WriteHandler(DatagramPacket initP) {
            this.clientAddress = initP.getSocketAddress();
            this.running = true;
            this.dataBuffer = new LinkedList<>();
            this.pathname = TftpPacket.extractPathname(initP);

            int tid = randomTid();
            boolean tryAndBind = true;

            // Search for a free tid to bind to
            while (tryAndBind) {
                try {
                    this.tempSocket = new DatagramSocket(tid);
                    tryAndBind = false;
                } catch (SocketException e) {
                    tid = randomTid();
                }
            }

            // Ack the initial packet, which should prompt the client to begin sending data
            writeConnections.put(initP.getSocketAddress(), this);
            acknowledge(initP);
        }

        @Override
        public void run() {
            DatagramPacket p;

            while (running) {
                // get a packet
                // TODO error handling
                System.out.println("Waiting for data to be sent to port " + tempSocket.getPort());
                p = rawReceive(tempSocket);

                // check p is from client
                if (!p.getSocketAddress().equals(clientAddress)) {
                    sendError(p, "You are not my client");
                    continue;
                }

                // If this is the last packet
                // save the data, disconnect, and stop running
                if (handleDataPacket(p, dataBuffer)) {
                     byte[] assembled = assembleData(dataBuffer);
                     saveData(pathname, assembled);

                     tempSocket.disconnect();
                     writeConnections.remove(p.getSocketAddress());
                     running = false;
                }
            }
        }
    }
}
