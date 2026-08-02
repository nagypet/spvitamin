package hu.perit.spvitamin.spring.security.xss;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;

class CachedBodyServletInputStream extends ServletInputStream
{
    private final ByteArrayInputStream delegate;

    CachedBodyServletInputStream(byte[] body)
    {
        this.delegate = new ByteArrayInputStream(body);
    }

    @Override
    public int read()
    {
        return delegate.read();
    }

    @Override
    public int read(byte[] b, int off, int len)
    {
        return delegate.read(b, off, len);
    }

    @Override
    public int available()
    {
        return delegate.available();
    }

    @Override
    public boolean isFinished()
    {
        return delegate.available() == 0;
    }

    @Override
    public boolean isReady()
    {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener)
    {
    }
}
