package myTftp;

public class AckTftpPacket extends TftpPacket {
    int blockNo;

    public AckTftpPacket(int blockNo) {
        super(4);
        this.blockNo = blockNo;
    }

    @Override
    public byte[] toBytes() {
        byte[] asBytes = new byte[TftpUser.TFTP_CAPACITY];

        asBytes[0] = (byte) getOpcode();
        asBytes[2] = (byte) blockNo;

        return asBytes;
    }
}
