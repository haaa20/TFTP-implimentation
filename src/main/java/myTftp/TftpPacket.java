package myTftp;


import java.net.DatagramPacket;
import java.util.Arrays;

public abstract class TftpPacket {
    public static byte[] OCTET_TRANSFER_MODE = "octet".getBytes();

    private int opcode;

    // Because this is an abstract class, every superclass should have its opcode set
    public TftpPacket(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return opcode;
    }

    public static int extractOpcode(byte[] bytePacket) {
        return bytePacket[0];
    }

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

    public static int extractOpcode(DatagramPacket p) {
        return extractOpcode(p.getData());
    }

    public abstract byte[] toBytes();
}
