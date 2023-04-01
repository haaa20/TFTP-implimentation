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
        else if (opcode > 4) {
            System.err.println("WARNING: unexpected opcode");
        }

        return bytePacket[2];
    }

    public static int extractPacketNo(DatagramPacket p) {
        return extractPacketNo(p.getData());
    }

    public static byte[] extractData(byte[] bytePacket, int len) {
        if (bytePacket[0] != 3) {
            System.err.println("WARNING: this isn't a DATA packet");
        }
        return Arrays.copyOfRange(bytePacket, 4, len);
    }

    public static byte[] extractData(DatagramPacket p) {
        byte[] bytePacket = p.getData();
        int len = p.getLength();
        return extractData(bytePacket, len);
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

    public static ErrorStruct checkError(DatagramPacket p) { return checkError(p.getData());}

    private static ErrorStruct checkError(byte[] data) {
        if (data[0] != 5) {
            return new ErrorStruct();
        }

        // Scan error message
        int i = 4;
        while (data[i] != (byte)0) {i++;}
        return new ErrorStruct(data[2], Arrays.copyOfRange(data, 4, i));
    }

    public abstract byte[] toBytes();
}
