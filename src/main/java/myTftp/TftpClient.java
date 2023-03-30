package myTftp;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class TftpClient extends TftpUser{
    public TftpClient(String name, int portNo) {
        super(name, portNo);
    }

    public boolean requestRead(InetAddress address, int portNo, String fileName) {
        request(address, portNo, fileName, WRMode.READ);

        return false;
    }

    public boolean requestWrite(InetAddress address, int portNo, String fileName) {
        request(address, portNo, fileName, WRMode.WRITE);

        return false;
    }

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
