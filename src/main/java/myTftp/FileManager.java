package myTftp;
import java.io.*;
import java.nio.file.Files;

public class FileManager {
    private String homepath;
    private File selectedFile;

    public FileManager(String homePath) {
        this.homepath = homePath;
    }

    public FileManager() {

    }

    /**
     * Returns the given file encoded as an array of bytes - ready for transmission
     *
     * @param pathname File name or path
     * @return file as byte[]
     */
    public byte[] read(String pathname) {
        selectFile(pathname);
        try {
            return Files.readAllBytes(selectedFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the contents withing the byte array to the path given. If no such path exists, then a file will be created,
     * otherwise teh contents of the file will be overwritten.
     *
     * @param pathname Path to be saved to
     * @param contents Data
     * @return
     */
    public boolean save(String pathname, byte[] contents) {
        selectFile(pathname);
        FileOutputStream fos;

        try {
            selectedFile.createNewFile();
            fos = new FileOutputStream(selectedFile);
            fos.write(contents);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void selectFile(String pathname) {
        selectedFile = new File(homepath + "/" + pathname);
    }

    public String getHomepath() {
        return homepath;
    }

    public void setHomepath(String homepath) {
        this.homepath = homepath;
    }

    public boolean exists(String pathname) {
        selectFile(pathname);
        return selectedFile.exists();
    }
}
