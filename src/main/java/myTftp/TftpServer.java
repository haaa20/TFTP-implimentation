package myTftp;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.*;

import static java.lang.Thread.sleep;

public class TftpServer extends TftpUser implements Runnable {
    private boolean running;
    private Map<SocketAddress, Iterator<byte[]>> readConnections;
    private Map<SocketAddress, WriteStruct> writeConnections;

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
                WriteStruct newWriteStruct = new WriteStruct(TftpPacket.extractPathname(p));
                writeConnections.put(p.getSocketAddress(), newWriteStruct);
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
        int length = data.length;
        WriteStruct writeStruct = writeConnections.get(p.getSocketAddress());

        writeStruct.add(data);

        if (length < TFTP_CAPACITY - 4) {
            // We're done!
            String completeData = writeStruct.toString();
            writeConnections.remove(p.getSocketAddress());
        }
    }


    public void terminate() {
        running = false;
    }

    /**
     * Just a lil' structure to keep path names and buffer lists in one place
     */
    private class WriteStruct {
        String pathname;
        List<byte[]> buffer;

        public WriteStruct(String pathname) {
            this.pathname = pathname;
            this.buffer = new LinkedList<>();
        }

        public void add(byte[] block) {
            buffer.add(block);
        }

        public String toString() {
            return new String(assembleData(buffer));
        }
    }
}
