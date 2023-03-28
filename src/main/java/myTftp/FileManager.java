package myTftp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileManager {
    private String homePath;
    private File selectedFile;
    private FileWriter writer;
    private Scanner reader;

    public FileManager(String homePath) {
        this.homePath = homePath;
    }

    public void open(String name) {
        selectedFile = new File(homePath+"/"+name);

        if (!selectedFile.exists()) {
            System.err.println("There was a problem: that file does not exist");
            return;
        }
    }

    public String readFull() {
        String strOut = "";

        try {
            reader = new Scanner(selectedFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("No file to read from");
        }

        strOut += reader.nextLine();
        while (reader.hasNextLine()) {
            strOut += "\n";
            strOut += reader.nextLine();
        }

        return strOut;
    }

    public void write(String data) {
        try {
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            System.err.println("Could not write to " + selectedFile.getPath());
        } catch (NullPointerException e) {
            System.err.println("Tried to write before opening a file");
        }
    }

    private Boolean createFile() {
        try {
            return selectedFile.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }
}
