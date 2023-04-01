package myTftp;

public class ErrorStruct {
    boolean error;
    int code;
    byte[] message;

    public ErrorStruct() {
        error = false;
        code = 0;
        message = new byte[0];
    }

    public ErrorStruct(int code, byte[] message) {
        this.code = code;
        this.message = message;
        this.error = true;
    }

    public boolean isError() {
        return error;
    }

    public int getCode() {
        return code;
    }

    public byte[] getMessage() {
        return message;
    }
}
