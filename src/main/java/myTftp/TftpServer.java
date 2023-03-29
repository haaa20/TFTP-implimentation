package myTftp;

public class TftpServer extends TftpUser implements Runnable {
    public TftpServer(String name, int portNo) {
        super(name, portNo);
    }

    private void serverLoop() {
        while (true) {
            rawReceive();
        }
    }

    @Override
    public void run() {

    }
}
