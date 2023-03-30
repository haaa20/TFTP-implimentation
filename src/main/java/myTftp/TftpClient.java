package myTftp;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class TftpClient extends TftpUser{
    public TftpClient(String name, int portNo) {
        super(name, portNo);
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
        request(address, portNo, fileName, WRMode.READ);

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
        request(address, portNo, fileName, WRMode.WRITE);

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

        p = new DatagramPacket(rq.toBytes(), TFTP_CAPACITY);
        rawSend(p);
        p = rawReceive();

        return false;
    }
}
