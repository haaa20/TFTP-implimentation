package myTftp;

public abstract class Packet {
    private Oppcode oppcode;

    // Because this is an abstract class, every superclass should have its opcode set
    public Packet(Oppcode oppcode) {}

    public abstract byte[] toBytes();
}
