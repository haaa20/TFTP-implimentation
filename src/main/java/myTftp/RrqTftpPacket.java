package myTftp;

public class RrqTftpPacket extends TftpPacket{
    private byte[] filename;

    public RrqTftpPacket(String filename) {
        super(1);
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
