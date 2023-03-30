package myTftp;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.*;

import static java.lang.Thread.sleep;

public class TftpServer extends TftpUser implements Runnable {
    private boolean running;
    private Map<SocketAddress, Iterator<byte[]>> readConnections;
    private Map<SocketAddress, List<byte[]>> writeConnections;

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

            if (op == 1) {
                // A new READ request
            }
            else if (op == 2) {
                // A new WRITE request
                // Map the request to a new buffer list, and acknowledge
                writeConnections.put(p.getSocketAddress(), new LinkedList<>());
                acknowledge(p);
            }
            else if (op == 3) {
                // A new DATA packet
                handleDataPacket(p);
                acknowledge(p);
            }
            else if (op == 4) {
                // A new ACK packet!
                // ... Which, remember, will be in response to an outgoing data packet!
            }
        }
    }

    private void handleDataPacket(DatagramPacket p) {
        // Assume that the socket has done its job and a buffer list is in place
        byte[] data = TftpPacket.extractData(p.getData(), p.getLength());
        List<byte[]> bufferList = writeConnections.get(p.getSocketAddress());
        int length = data.length;

        bufferList.add(data);

        if (length < TFTP_CAPACITY - 4) {
            // We're done!
            String completeData = new String(assembleData(bufferList));
            writeConnections.remove(p.getSocketAddress());
        }
    }


    public void terminate() {
        running = false;
    }

}
