package myTftp;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.*;

import static java.lang.Thread.sleep;

public class TftpServer extends TftpUser implements Runnable {
    private boolean running;
    private Map<SocketAddress, ReadStruct> readConnections;
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

            String s = new String(p.getData());

            if (op == 1) {
                // A new READ request
                // Map the request to a new string and send the first block
                say("received read request");
                String pathname = TftpPacket.extractPathname(p);
                ReadStruct newReadStruct = new ReadStruct(pathname);
                SocketAddress socketAddress = p.getSocketAddress();

                readConnections.put(socketAddress, newReadStruct);

                sendNextData(socketAddress, newReadStruct);
            }
            else if (op == 2) {
                // A new WRITE request
                // Map the request to a new buffer list, and acknowledge
                say("received write request");
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
                handleAckPacket(p);
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
            byte[] completeData = writeStruct.complete();
            saveData(writeStruct.pathname, completeData);
            writeConnections.remove(p.getSocketAddress());
        }
    }

    private boolean handleAckPacket(DatagramPacket p) {
        SocketAddress address = p.getSocketAddress();
        ReadStruct rs = readConnections.get(address);
        sendNextData(address, rs);
        return true;
    }

    private void sendNextData(SocketAddress address, ReadStruct rs) {
        byte[] data = rs.next();
        data = new DataTftpPacket(rs.i, data).toBytes();
        DatagramPacket p = new DatagramPacket(data, data.length);
        p.setSocketAddress(address);
        say("sending packet no." + rs.i);
        rawSend(p);
    }

    public void terminate() {
        running = false;
    }

    private class ReadStruct {
        Iterator<byte[]> iterator;
        int i;

        public ReadStruct(String pathname) {
            this.iterator = windowIterator(pathname);
            this.i = 0;
        }

        public byte[] next() {
            i++;
            return iterator.next();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }
    }

    /**
     * Just a li'l structure to keep path names and buffer lists in one place
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

        public byte[] complete() {
            return assembleData(buffer);
        }
    }
}
