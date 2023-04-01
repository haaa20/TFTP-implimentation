package myTftp;

public class ErrTftpPacket extends TftpPacket {
    private int errorCode;
    private byte[] errorMessage;

    public ErrTftpPacket(int errorCode, String errorMessage) {
        super(5);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage.getBytes();
    }

    @Override
    public byte[] toBytes() {
        byte[] asBytes = new byte[TftpUser.TFTP_CAPACITY];

        asBytes[0] = (byte) getOpcode();
        asBytes[2] = (byte) errorCode;
        System.arraycopy(errorMessage, 0, asBytes, 4, errorMessage.length);

        return asBytes;
    }

}
