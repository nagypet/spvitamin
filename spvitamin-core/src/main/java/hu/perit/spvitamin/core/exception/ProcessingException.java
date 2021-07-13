package hu.perit.spvitamin.core.exception;

/**
 * ProcessingException
 * @author Peter Nagy (xgxtpna)
 */
public class ProcessingException extends RuntimeException
{

    private static final long serialVersionUID = 777514113674176800L;

    /**
     * @param message
     * @param cause
     */
    public ProcessingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ProcessingException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public ProcessingException(Throwable cause)
    {
        super(cause);
    }
}
