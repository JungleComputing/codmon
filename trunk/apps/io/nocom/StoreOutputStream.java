/* $Id: StoreOutputStream.java,v 1.2 2007/05/24 16:27:41 ceriel Exp $ */


import java.io.IOException;
import java.io.OutputStream;

final class StoreOutputStream extends OutputStream {

    int len = 0;

    StoreBuffer buf;

    public StoreOutputStream(StoreBuffer buf) {
        this.buf = buf;
    }

    public int getAndReset() {
        int temp = len;
        len = 0;
        return temp;
    }

    public void close() throws IOException {
    }

    public void flush() throws IOException {
    }

    public void write(byte[] b) throws IOException {
        len += b.length;
        buf.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.len += len;
        buf.write(b, off, len);
    }

    public void write(int b) throws IOException {
        len += 1;
        buf.write(b);
    }
}

