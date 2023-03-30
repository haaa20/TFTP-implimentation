package myTftp;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class TftpClient extends TftpUser{
    public TftpClient(String name, int portNo) {
        super(name, portNo);
        setTimeout(5000);
    }

    /**
     * Sends a request to read to the TftpUser at the given address
     *
     * @param address InetAddress
     * @param portNo Integer port number
     * @param fileName the name of the file (this includes extension)
     * @return True if request granted
     */
    public boolean requestRead(InetAddress address, int portNo, String fileName) {
        if (request(address, portNo, fileName, WRMode.READ)) {
            return true;
        }

        return false;
    }

    /**
     * Sends a request to write to the TftpUser at the given address
     *
     * @param address InetAddress
     * @param portNo Integer port number
     * @param fileName the name of the file (this includes extension)
     * @return True if request granted
     */
    public boolean requestWrite(InetAddress address, int portNo, String fileName) {
        if (request(address, portNo, fileName, WRMode.WRITE)){
            sendFile(address, portNo, fileName);
            return true;
        }

        return false;
    }

    // As the methods requestRead() and requestWrite() are so similar, they're both basically calls to
    // the below with slightly different parameters
    private boolean request(InetAddress address, int portNo, String fileName, WRMode mode) {
        TftpPacket rq;
        DatagramPacket p;
        byte[] response;

        if (mode == WRMode.READ) {
            rq = new RrqTftpPacket(fileName);
        }
        else { // mode == WRMode.WRITE
            rq = new WrqTftpPacket(fileName);
        }

        // Send out the request package and await acknowledgment
        byte[] buf = rq.toBytes();
        p = new DatagramPacket(buf, buf.length);
        p.setAddress(address);
        p.setPort(portNo);
        rawSend(p);
        p = rawReceive();

        // Check that the packet we received is what we expected:
        // an ack packet of block no. 0
        // from the same address we sent the request to
        if (!p.getAddress().equals(address) || p.getPort() != portNo) {
            sendError(p, "You are not who I expected");
            return false;
        }
        else if (TftpPacket.extractPacketNo(p.getData()) != 0) {
            return false;
        }
        return true;
    }
}
