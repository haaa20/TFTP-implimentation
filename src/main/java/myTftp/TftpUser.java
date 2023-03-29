package myTftp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;


public class TftpUser {
    public static int TFTP_CAPACITY = 512;

    private String name;
    private DatagramPacket packet;
    private DatagramSocket socket;
    private byte[] buf;

    public TftpUser(String name, int portNo) {
        this.name = name;
        this.buf = new byte[TFTP_CAPACITY];
        this.packet = new DatagramPacket(buf, TFTP_CAPACITY);

        try {
            this.socket = new DatagramSocket(portNo);
        } catch (SocketException e) {
            System.err.println("WARNING: " + name + " could not set up the socket correctly");
        }

    }

    /**
     * Send an array of bytes in one or more data packets
     * <p>
     * Most efficient of the three overloads
     *
     * @param data array
     */
    public void sendData(InetAddress serverAddress, int portNo, byte[] data) {
        int finalPacketNo = calculateNumOfWindows(data.length);

        for (int i = 0; i < finalPacketNo; i++) {
            say("Sending packet no." + i);
            byte[] dataBlock = dataWindow(data, i);
            sendSingleData(serverAddress, portNo, dataBlock, i);
        }
    }

    /**
     * Send an array of bytes in one or more data packets
     *
     * @param data array
     */
    public void sendData(InetAddress address, int portNo, Byte[] data) {
        sendData(address, portNo, unwrapByteArray(data));
    }

    /**
     * Send a list of bytes in one or more data packets
     *
     * @param data List
     */
    public void sendData(InetAddress address, int portNo, List<Byte> data) {
        sendData(address, portNo, data.toArray(new Byte[data.size()]));
    }

    /**
     * Receives data and writes it to a list in blocks
     *
     * @param dataStream The list to chick the blocks of data are to be added
     * @return true if all data was received successfully
     */
    public boolean receiveData(List<byte[]> dataStream) {
        // Receive and acknowledge the first packet - there will (should) always be at least one
        // Extract the first data block
        byte[] data = receiveSingleData();
        dataStream.add(data.clone());

        // So long as we are yet to receive a packet of below maximum length, there is more data coming!
        while (data.length < TFTP_CAPACITY) {
            data = receiveSingleData();
            dataStream.add(data.clone());
        }
        return true;
    }

    /**
     * Sends a single data packet and waits for a response
     *
     * @param address InetAddress
     * @param port port number
     *
     * @return 0 if correct ack received, -1 if no ack received, n if incorrect nth ack received
     */
    public int sendSingleData(InetAddress address, int port, byte[] data, int blockNo){
        // Check the data is valid
        if (data.length > TFTP_CAPACITY - 4) {
            System.err.println("Too much data!");
        }

        // getting the data ready
        DataTftpPacket dataPacket = new DataTftpPacket(blockNo, data);
        buf = dataPacket.toBytes();

        // Set the data, address, and port number
        packet.setAddress(address);
        packet.setPort(port);
        packet.setData(buf);

        // Send the packet out through the socket
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("There was a problem sending this packet");
            return -1;
        }

        // wait for an acknowledgement
        try {
            socket.receive(packet);
        } catch (IOException e) {
            System.err.println("Cannot receive acknowledgement");
            return -1;
        }
        buf = packet.getData();
        int n = TftpPacket.extractPacketNo(buf);
        if (n == blockNo) {
            return 0;
        }
        else {
            return n;
        }
    }

    /**
     * Waits until a packet is received through the socket, and sends an acknowledgement
     */
    public byte[] receiveSingleData() {
        try {
            socket.receive(packet);
            acknowledge(packet);
            int len = packet.getLength();

            return TftpPacket.extractData(buf, len);
        } catch (IOException e) {
            throw new RuntimeException("There was a problem receiving this packet");
        }
    }

    /**
     * Acknowledge the given packet (do this IMMEDIATELY after the packet is received to minimise spaghetti please)
     *
     * @param p DatagramPacket
     * @throws IOException IDK if there's a problem
     */
    protected void acknowledge(DatagramPacket p) throws IOException {
        // Preparing the ack packet
        AckTftpPacket ackData = new AckTftpPacket(TftpPacket.extractPacketNo(p.getData()));
        DatagramPacket ackPacket = new DatagramPacket(ackData.toBytes(), 4);

        // Addressing the acknowledgement packet
        ackPacket.setAddress(p.getAddress());
        ackPacket.setPort(p.getPort());

        // Sending the ack packet
        socket.send(ackPacket);
    }

    /**
     * returns the ith data window - where each window is size 408 bytes or less
     *
     * @param data the data
     * @param winNo i
     * @return the window, or an empty array
     */
    public static byte[] dataWindow(List<Byte> data, int winNo) {
        int winStart = (TFTP_CAPACITY - 4)*winNo;
        int winEnd = (TFTP_CAPACITY - 4)*(winNo+1);
        Byte[] window = new Byte[TFTP_CAPACITY - 4];
        int dataSize = data.size();

        if (winEnd < dataSize) {
            // The window is not the last
            window = data.subList(winStart, winEnd).toArray(window);
            return unwrapByteArray(window);
        }
        else if (winStart <= dataSize) {
            // The winEnd is beyond the end of the data, but the start is at or before
            window = data.subList(winStart, dataSize).toArray(window);
            return unwrapByteArray(window);
        }
        else {
            // winStart is at the end of the data: the window is empty
            return new byte[0];
        }
    }

    /**
     * returns the ith data window - where each window is size 408 bytes or less
     *
     * @param data the data
     * @param winNo i
     * @return the window, or an empty array
     */
    public static byte[] dataWindow(byte[] data, int winNo) {
        int winStart = (TFTP_CAPACITY - 4)*winNo;
        int winEnd = (TFTP_CAPACITY - 4)*(winNo+1);

        if (winEnd < data.length) {
            // The window is not the last
            return Arrays.copyOfRange(data, winStart, winEnd);
        }
        else if (winStart < data.length) {
            // The winEnd is beyond the end of the data, but the start is at or before
            return Arrays.copyOfRange(data, winStart, data.length);
        }
        else {
            // winStart is at the end of the data: the window is empty
            return new byte[0];
        }
    }

    /**
     * Splits a large list of bytes into segments of the right size to fit in a TFTP data packet - IE 508 byte chunks
     *
     * @param data List of Bytes
     * @return An Iterator object
     */
    public static Iterator<Byte[]> segmentData(List<Byte> data) {
        // I'm feeling brave let's do an anonymous class ha ha ha!!
        return new Iterator<Byte[]>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return (i < data.size());
            }

            @Override
            public Byte[] next() {
                Object[] dataSegment = data.subList(i, Math.min(i+408, data.size())).toArray();
                i += 408;
                return Arrays.copyOf(dataSegment, dataSegment.length, Byte[].class);
            }
        };
    }

    /**
     * Unwraps the contents of Bytes to an array of primitive bytes
     *
     * @param a Array of Bytes
     * @return byte[]
     */
    public static byte[] unwrapByteArray(Byte[] a) {
        byte[] b = new byte[a.length];
        int i = 0;

        for (Byte wrapped : a) {
            b[i++] = wrapped;
        }

        return b;
    }

    /**
     * Wraps the contents of bytes to an array
     *
     * @param a Array of Bytes
     * @return byte[]
     */
    public static Byte[] wrapByteArray(byte[] a) {
        Byte[] b = new Byte[a.length];
        int i = 0;

        for (byte p : a) {
            b[i++] = p;
        }

        return b;
    }

    /**
     * Does what is says in the tin I'm not explaining it I'm in enough of a rush as it is
     */
    private static int calculateNumOfWindows(int dataLen) {
        int winNo = dataLen / 508;
        if (winNo % 508 == 0) {winNo++;}
        return winNo;
    }

    /**
     * Prints a message to the terminal along with the user's name - primarily for debugging
     * @param message
     */
    public void say(String message) {
        System.out.println(name + ": " + message);
    }

}
