package myTftp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

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

        // Set the address and port number of the system to which the packet is being sent
        packet.setAddress(address);
        packet.setPort(port);

        // getting the data ready
        DataTftpPacket dataPacket = new DataTftpPacket(blockNo, data);
        buf = dataPacket.toBytes();

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
    public byte[] receiveData() {
        // The array where we will store the retrieved data, ready to be returned
        byte[] receivedData = new byte[TFTP_CAPACITY - 4];

        try {
            socket.receive(packet);

            // Store the address and port from which the inbound packet was sent, ready for acknowledgement
            // also extract the block no
            InetAddress senderAddress = packet.getAddress();
            int senderPort = packet.getPort();

            // Extracting the data and packet no
            buf = packet.getData();
            int ackNo = TftpPacket.extractPacketNo(buf);
            receivedData = TftpPacket.extractData(buf);

            // Preparing the ack packet
            AckTftpPacket ack = new AckTftpPacket(ackNo);
            buf = ack.toBytes();
            packet.setAddress(senderAddress);
            packet.setPort(senderPort);
            packet.setData(buf);

            // Sending the ack packet
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("There was a problem receiving this packet");
        }

        return receivedData;
    }
}
