package myTftp;

public class RrqTftpPacket extends TftpPacket{
    public RrqTftpPacket() {
        super(1);
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
