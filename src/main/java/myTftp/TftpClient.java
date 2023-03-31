package myTftp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

public class TftpClient extends TftpUser{
    private List<byte[]> readBuffer;
    public TftpClient(String name, int portNo) {
        super(name, portNo);
        setTimeout(2000);
        readBuffer = new LinkedList<>();
    }

    /**
     * Sends a request to read to the TftpUser at the given address
     *
     * @param address InetAddress
     * @param portNo Integer port number
     * @param pathname the name of the file (this includes extension)
     * @return True if request granted
     */
    public boolean requestRead(InetAddress address, int portNo, String pathname) {
        DatagramPacket p = request(address, portNo, pathname, WRMode.READ);

        receiveData(readBuffer);
        saveData(pathname, assembleData(readBuffer));

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
        DatagramPacket p = request(address, portNo, fileName, WRMode.WRITE);

        // Check that the packet we received is what we expected:
        // i.e. an ack packet of block no. 0
        // from the same address we sent the request to
        if (!p.getAddress().equals(address) || p.getPort() != portNo) {
            sendError(p, "You are not who I expected");
            return false;
        }
        else if (TftpPacket.extractPacketNo(p.getData()) != 0) {
            return false;
        }

        sendFile(address, portNo, fileName);

        return false;
    }

    // As the methods requestRead() and requestWrite() are so similar, they're both basically calls to
    // the below with slightly different parameters
    private DatagramPacket request(InetAddress address, int portNo, String fileName, WRMode mode) {
        TftpPacket rq;
        DatagramPacket p;
        byte[] response;

        if (mode == WRMode.READ) {
            rq = new RrqTftpPacket(fileName);
        }
        else { // mode == WRMode.WRITE
            rq = new WrqTftpPacket(fileName);
        }

        // Send out the request package and await a response
        byte[] buf = rq.toBytes();
        p = new DatagramPacket(buf, buf.length);
        p.setAddress(address);
        p.setPort(portNo);
        rawSend(p);
        p = rawReceive();

        // A null packet signifies that our request timed out, try again!
        while (p == null) {
            request(address, portNo, fileName, WRMode.WRITE);
            p = rawReceive();
        }
        return p;
    }
}
