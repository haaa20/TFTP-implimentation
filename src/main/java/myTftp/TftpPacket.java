package myTftp;


import java.util.Arrays;

public abstract class TftpPacket {
    private Oppcode oppcode;

    // Because this is an abstract class, every superclass should have its opcode set
    public TftpPacket(Oppcode oppcode) {}

    public static int extractPacketNo(byte[] bytePacket) {
        if (bytePacket[0] != 3 && bytePacket[0] != 4) {
            System.err.println("WARNING: this isn't an ACK or DATA packet");
        }

        return bytePacket[2];
    }

    public static byte[] extractData(byte[] bytePacket, int len) {
        if (bytePacket[0] != 3) {
            System.err.println("WARNING: this isn't a DATA packet");
        }
        return Arrays.copyOfRange(bytePacket, 4, len);
    }

    public abstract byte[] toBytes();
}
