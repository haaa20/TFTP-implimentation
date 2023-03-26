package myTftp;

public class DataPacket extends Packet {
    private int blockNo;
    private byte[] data;
    private boolean end;

    public DataPacket(int blockNo, byte[] data) {
        super(Oppcode.DATA);
        this.blockNo = blockNo;
        this.data = data;

        if (data.length < TftpUser.TFTP_CAPACITY - 4) {
            this.end = true;
        }
        else if (data.length > TftpUser.TFTP_CAPACITY - 4) {
            System.err.println("WARNING:\npassed too much data to a single data packet, some will be lost!");
        }

    }

    @Override
    public byte[] toBytes() {
        byte [] asBytes = new byte[512];

        // Setting the opcode and block no
        asBytes[0] = 3;
        asBytes[2] = (byte) blockNo; // NAIVE what if blockNo > 255...!?!?!

        // Copying the data across
        System.arraycopy(data, 0, asBytes, 4, Math.min(TftpUser.TFTP_CAPACITY - 4, data.length));

        return asBytes;
    }
}
