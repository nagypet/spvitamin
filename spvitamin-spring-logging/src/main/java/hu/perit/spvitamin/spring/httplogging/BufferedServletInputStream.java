package hu.perit.spvitamin.spring.httplogging;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;

final class BufferedServletInputStream extends ServletInputStream
{

    private final ByteArrayInputStream bais;

    public BufferedServletInputStream(ByteArrayInputStream bais)
    {
        this.bais = bais;
    }

    public int available()
    {
        return this.bais.available();
    }

    public int read()
    {
        return this.bais.read();
    }

    public int read(byte[] buf, int off, int len)
    {
        return this.bais.read(buf, off, len);
    }

    public boolean isFinished()
    {
        return false;
    }

    public boolean isReady()
    {
        return true;
    }

    public void setReadListener(ReadListener readListener)
    {
    }
}
