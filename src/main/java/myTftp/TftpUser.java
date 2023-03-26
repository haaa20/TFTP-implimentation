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

    public TftpUser(String name, int portNo) throws SocketException {
        this.name = name;
        this.socket = new DatagramSocket(portNo);
        this.buf = new byte[TFTP_CAPACITY];
        this.packet = new DatagramPacket(buf, TFTP_CAPACITY);
    }

    /**
     * Sends the packet on its way
     *
     * @param address InetAddress
     * @param port port number
     */
    public void send(InetAddress address, int port) {
        // Set the address and port number of the system to which the packet is being sent
        packet.setAddress(address);
        packet.setPort(port);

        // ADDING SOME DUMMY DATA TO BUF
        byte[] dummyData = "DUMMY DATA".getBytes();
        System.arraycopy(dummyData, 0, buf, 0, dummyData.length);

        // Send the packet out through the socket
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("There was a problem sending this packet");
        }
    }

    /**
     * Waits until a packet is received through the socket, and sends an acknowledgement
     */
    public void receive() {
        try {
            socket.receive(packet);
        } catch (IOException e) {
            System.err.println("There was a problem receiving this packet");
        }
    }
}
