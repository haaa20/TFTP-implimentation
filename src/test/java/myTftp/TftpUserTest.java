package myTftp;

import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TftpUserTest {
    @Test
    void name() {
        List<Byte> bytes = new ArrayList<>();
        byte[] chunk1 = new byte[408];
        byte[] chunk2 = new byte[408];

        for (int i = 0; i < 408; i++){
            bytes.add((new Byte("1")));
            chunk1[i] = new Byte("1");
        }

        for (int i = 0; i < 408; i++){
            bytes.add((new Byte("2")));
            chunk2[i] = new Byte("2");
        }

        Iterator<Byte[]> iter = TftpUser.segmentData(bytes);

        assertArrayEquals(chunk1, TftpUser.unwrapByteArray(iter.next()));
        assertArrayEquals(chunk2, TftpUser.unwrapByteArray(iter.next()));
    }
}