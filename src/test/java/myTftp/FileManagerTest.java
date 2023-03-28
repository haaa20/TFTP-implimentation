package myTftp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileManagerTest {
    @Test
    void name() {
        FileManager fileManager = new FileManager("files");
        fileManager.open("tea.txt");
        System.out.println(fileManager.readFull());
    }
}