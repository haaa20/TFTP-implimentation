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
        byte opcode = bytePacket[0];
        if (opcode == 1 || opcode == 2) {
            return 0;
        }
        if (opcode < 4) {
            System.err.println("WARNING: unexpected opcode");
        }

        return bytePacket[2];
    }

    public static byte[] extractData(byte[] bytePacket, int len) {
        if (bytePacket[0] != 3) {
            System.err.println("WARNING: this isn't a DATA packet");
        }
        return Arrays.copyOfRange(bytePacket, 4, len);
    }

    public static String extractPathname(byte[] bytePacket) {
        int opcode = extractOpcode(bytePacket);
        int i = 2;
        if (opcode != 1 && opcode != 2) {
            System.err.println("WARNING: This isn't a WRQ or RRQ packet");
        }

        // iterate through bytePacket from i until we find the 0 byte that signifies the end of the pathname
        while (bytePacket[i] != 0) {i++;}

        return new String(Arrays.copyOfRange(bytePacket, 2, i));
    }

    public static int extractOpcode(DatagramPacket p) {
        return extractOpcode(p.getData());
    }

    public static String extractPathname(DatagramPacket p) {
        return extractPathname(p.getData());
    }

    public abstract byte[] toBytes();
}
