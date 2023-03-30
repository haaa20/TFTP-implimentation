package myTftp;

public class WrqTftpPacket extends TftpPacket {
    private byte[] filename;

    public WrqTftpPacket(String filename) {
        super(2);
        this.filename = filename.getBytes();
    }

    @Override
    public byte[] toBytes() {
        byte[] asBytes = new byte[filename.length + 4];

        asBytes[0] = (byte) getOpcode();
        System.arraycopy(filename, 0, asBytes, 2, filename.length);

        return asBytes;
    }
}
