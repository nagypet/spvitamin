package hu.perit.spvitamin.core.timeoutlatch;

public class TimeoutLatch
{
    private final Long hysteresisMillis;

    private boolean opened;
    private long timeClosing;

    public TimeoutLatch(Long hysteresisMillis)
    {
        this.hysteresisMillis = hysteresisMillis;
        this.opened = true;
    }


    public synchronized void setClosed()
    {
        this.opened = false;
        this.timeClosing = System.currentTimeMillis();
    }

    public synchronized boolean isOpen()
    {
        if (this.opened)
        {
            return true;
        }

        if ((System.currentTimeMillis() - this.timeClosing) > hysteresisMillis)
        {
            this.opened = true;
            return true;
        }

        return false;
    }

    public boolean isClosed()
    {
        return !isOpen();
    }
}
