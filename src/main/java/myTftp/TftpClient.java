package myTftp;

public class TftpClient extends TftpUser{
    public TftpClient(String name, int portNo) {
        super(name, portNo);
    }

    public boolean requestRead(String fileName) {
        return false;
    }

    public boolean requestWrite(String fileName) {
        return false;
    }
}
