package myTftp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

public class FileManager {
    private String homePath;
    private File selectedFile;

    public FileManager(String homePath) {
        this.homePath = homePath;
    }

    public FileManager() {

    }

    /**
     * Returns the given file encoded as an array of bytes - ready for transmission
     *
     * @param file File name or path
     * @return file as byte[]
     */
    public byte[] read(String file) {
        selectFile(file);
        try {
            return Files.readAllBytes(selectedFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String dest) {

    }

    public void write() {
        write("");
    }

    private void selectFile(String filePath) {
        selectedFile = new File(homePath + "/" + filePath);
    }

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }
}
