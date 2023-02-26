package hu.perit.spvitamin.spring.httplogging;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public class TeeServletOutputStream extends ServletOutputStream
{

    private final TeeOutputStream targetStream;

    public TeeServletOutputStream(OutputStream one, OutputStream two)
    {
        this.targetStream = new TeeOutputStream(one, two);
    }

    public void write(int arg0) throws IOException
    {
        this.targetStream.write(arg0);
    }

    public void flush() throws IOException
    {
        super.flush();
        this.targetStream.flush();
    }

    public void close() throws IOException
    {
        super.close();
        this.targetStream.close();
    }

    public boolean isReady()
    {
        return false;
    }

    public void setWriteListener(WriteListener writeListener)
    {
    }
}
