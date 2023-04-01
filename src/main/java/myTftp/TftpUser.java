package myTftp;

import java.io.IOException;
import java.net.*;
import java.util.*;


public abstract class TftpUser {
    public static int TFTP_CAPACITY = 512;

    private int originalPortNo;
    private String name;
    private DatagramPacket packet;
    private DatagramSocket socket;
    private byte[] buf;
    private boolean debug;
    private FileManager fileManager;
    private int timeout;

    public TftpUser(String name, int portNo) {
        this.name = name;
        this.buf = new byte[TFTP_CAPACITY];
        this.packet = new DatagramPacket(buf, TFTP_CAPACITY);
        this.fileManager = new FileManager();
        this.debug = false;

        try {
            this.socket = new DatagramSocket(portNo);
        } catch (SocketException e) {
            System.err.println("WARNING: " + name + " could not set up the socket correctly");
        }
        this.originalPortNo = portNo;
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
        say("I should be sending " + finalPacketNo + " packets");

        for (int i = 0; i <= finalPacketNo; i++) {
            byte[] dataBlock = dataWindow(data, i);
            say("Sending packet no." + (i + 1) + " of length " + dataBlock.length);
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
     * Sends the contents of the given file
     *
     * @param address recipient address
     * @param portNo recipient port number
     * @param pathname path of the file to be sent
     */
    public void sendFile(InetAddress address, int portNo, String pathname) {
        byte[] read = fileManager.read(pathname);
        String s = new String(read);
        sendData(address, portNo, read);
    }

    /**
     * Receives data and writes it to a list in blocks, starting from packet a given packet number, usefull
     * if one or more packets has already been received.
     *
     * @param dataStream The list to chick the blocks of data are to be added
     * @return true if all data was received successfully
     */
    public boolean receiveData(List<byte[]> dataStream, int initial) {
        // Receive and acknowledge the first packet - there will (should) always be at least one
        // Extract the first data block
        byte[] data = receiveSingleData();
        dataStream.add(data.clone());
        int i = initial;

        // So long as we are yet to receive a packet of below maximum length, there is more data coming!
        while (data.length >= TFTP_CAPACITY - 4) {
            say("Expecting another packet...");
            data = receiveSingleData();
            dataStream.add(data.clone());
            i++;
        }
        return true;
    }

    /**
     * Receives data and writes it to a list in blocks
     *
     * @param dataStream The list to chick the blocks of data are to be added
     * @return true if all data was received successfully
     */
    public boolean receiveData(List<byte[]> dataStream) {
        return receiveData(dataStream, 1);
    }

    /**
     * waits for data to be sent, and reassembles it for the packets
     *
     * @return The data as a continuous byte array
     */
    public byte[] receiveAndAssemble() {
        List<byte[]> blockBuf = new ArrayList();
        receiveData(blockBuf);
        return assembleData(blockBuf);
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
        if (!rawSend(packet)) {
            return -1;
        }

        // wait for an acknowledgement
        packet = rawReceive();

        // time for some ERROR HANDLING
        while (packet == null) {
            packet = freshPacket();
            packet.setAddress(address);
            packet.setPort(port);

            if (!rawSend(packet)) {
                return -1;
            }

            packet = rawReceive();
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
        DatagramPacket p = rawReceive();
        acknowledge(p);
        int len = packet.getLength();

        return TftpPacket.extractData(buf, len);
    }

    /**
     * Takes a list of data chunks and assembles them to a continuous array of bytes
     * <p>
     * Designed to be used on a data stream that has been written to by the receiveData method. if the list does not
     * conform to the expected structure an exception may be thrown
     *
     * @param dataStream The list of data chunks
     * @return The reassembled array of bytes
     */
    public byte[] assembleData(List<byte[]> dataStream) {
        int i = 0;
        int n = dataStream.size() - 1;
        int lastLength = dataStream.get(n).length;
        byte[] assembledData = new byte[n * (TFTP_CAPACITY - 4) + lastLength];

        for (byte[] block: dataStream) {
            System.arraycopy(block, 0, assembledData, i * (TFTP_CAPACITY - 4), block.length);
            i++;
        }

        return assembledData;
    }

    /**
     * Acknowledge the given packet (do this IMMEDIATELY after the packet is received to minimise spaghetti please)
     *
     * @param p DatagramPacket
     * @throws IOException IDK if there's a problem
     */
    protected void acknowledge(DatagramPacket p) {
        // Preparing the ack packet
        int ackNo = TftpPacket.extractPacketNo(p);
        DatagramPacket ackPacket = newAck(p.getSocketAddress(), ackNo);

        // Sending the ack packet
        rawSend(ackPacket);
    }

    protected final DatagramPacket newAck(SocketAddress address, int blockNo) {
        AckTftpPacket ackData = new AckTftpPacket(blockNo);
        DatagramPacket ackPacket = new DatagramPacket(ackData.toBytes(), 4);
        say("Received packet no." + blockNo);

        // Addressing the acknowledgement packet
        ackPacket.setSocketAddress(address);
        return ackPacket;
    }

    public boolean saveData(String pathname, byte[] contents) {
        return fileManager.save(pathname, contents);
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
    public byte[] dataWindow(byte[] data, int winNo) {
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
     * returns the ith data window - where each window is size 408 bytes or less
     *
     * @param pathname the path to be read from
     * @param winNo i
     * @return the window, or an empty array
     */
    public byte[] dataWindow(String pathname, int winNo) {
        return dataWindow(fileManager.read(pathname), winNo);
    }

    /**
     * Splits a large list of bytes into segments of the right size to fit in a TFTP data packet - IE 508 byte chunks
     *
     * @param data List of Bytes
     * @return An Iterator object
     */
    public Iterator<byte[]> windowIterator(byte[] data) {
        // I'm feeling brave let's do an anonymous class ha ha ha!!
        return new Iterator<byte[]>() {
            int i = 0;
            int n = calculateNumOfWindows(data.length);

            @Override
            public boolean hasNext() {
                return (i == n);
            }

            @Override
            public byte[] next() {
                return dataWindow(data, i++);
            }
        };
    }

    public Iterator<byte[]> windowIterator(String pathname) {
        return windowIterator(fileManager.read(pathname));
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
     * Sends the given packet out of the socket
     *
     * @param p DatagramPacket
     * @return True if packet successfully sent out
     */
    protected final boolean rawSend(DatagramPacket p) {
        // This SHOULD be the only method to directly use socket.send()
        try {
            socket.send(p);
            return true;
        } catch (IOException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }
    /**
     * Sends the given packet out of the  given socket
     *
     * @param p DatagramPacket
     * @param s Socket
     * @return True if packet successfully sent out
     */
    protected final boolean rawSend(DatagramPacket p, DatagramSocket s) {
        // This SHOULD be the only method to directly use socket.send()
        try {
            s.send(p);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Waits to receive a packet, and returns it. Does not acknowledge. Returns null if unsuccessful
     */
    protected final DatagramPacket rawReceive() {
        // This SHOULD be the only method to directly use socket.receive()
        try {
            setSocketTimeout(socket);
            socket.receive(packet);
        } catch (IOException e) {
            return null;
        }
        return packet;
    }

    /**
     * Waits to receive a packet from the given socket, and returns it.
     * Does not acknowledge. Returns null if unsuccessful
     */
    protected final DatagramPacket rawReceive(DatagramSocket tempSocket) {
        // This SHOULD be the only method to directly use socket.receive()
        try {
            tempSocket.receive(packet);
        } catch (IOException e) {
            return null;
        }
        return packet;
    }

    /**
     * Sends out an error package in response to p - usually done when p was unexpected
     *
     * @param p problem DatagramPacket
     */
    protected final void sendError(DatagramPacket p, String errorMessage) {
        // TODO
    }

    /**
     * Prints a message to the terminal along with the user's name - primarily for debugging
     * @param message
     */
    public final void say(String message) {
        if (!debug) {return;}
        System.out.println(name + ": " + message);
    }

    protected final int randomTid() {
        int tid = (int) (originalPortNo + Math.random()*500);
        return tid;
    }

    /**
     * Reassigns the socket of this user to the given port number
     *
     * @param portNo Port number
     * @return True if successfully rebound
     */
    public final boolean setPort(int portNo) {
        try {
            socket = new DatagramSocket(portNo);
            return true;
        } catch (SocketException e) {
            return false;
        }
    }

    /**
     * resets the port to the original port number
     * @return true if successful
     */
    public final boolean resetPort() {
        return setPort(originalPortNo);
    }

    public int getOriginalPortNo() {
        return originalPortNo;
    }

    public void setOriginalPortNo(int originalPortNo) {
        this.originalPortNo = originalPortNo;
    }

    protected final byte[] readLocal(String pathname) {
        return fileManager.read(pathname);
    }

    public void setFileHomePath(String homePath) {
        fileManager.setHomepath(homePath);
    }

    public String getFileHomePath(String homePath) {
        return fileManager.getHomepath();
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setTimeout(int milliseconds) {
        this.timeout = milliseconds;
    }

    private boolean setSocketTimeout(DatagramSocket s) {
        try {
            s.setSoTimeout(timeout);
            return true;
        } catch (SocketException e) {
            return false;
        }
    }

    // Do they want to read or write?
    protected enum WRMode {READ, WRITE}

    protected final DatagramPacket freshPacket() {
        return new DatagramPacket(buf, TFTP_CAPACITY);
    }
}
