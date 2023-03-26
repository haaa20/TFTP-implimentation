package myTftp;

public class AckPacket extends Packet{
    int blockNo;

    public AckPacket(int blockNo) {
        super(Oppcode.ACK);
        this.blockNo = blockNo;
    }

    @Override
    public byte[] toBytes() {
        byte[] asBytes = new byte[TftpUser.TFTP_CAPACITY];

        asBytes[0] = 4;
        asBytes[2] = (byte) blockNo;

        return asBytes;
    }
}
