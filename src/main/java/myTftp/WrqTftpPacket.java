package myTftp;

public class WrqTftpPacket extends TftpPacket {
    public WrqTftpPacket() {
        super(2);
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
