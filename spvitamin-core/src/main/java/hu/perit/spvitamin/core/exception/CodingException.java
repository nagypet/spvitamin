package hu.perit.spvitamin.core.exception;

/**
 * CodingException
 * @author Peter Nagy (xgxtpna)
 */
public class CodingException extends RuntimeException
{
    private static final long serialVersionUID = -4716621524523393946L;

    /**
     * @param message
     * @param cause
     */
    public CodingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message
     */
    public CodingException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public CodingException(Throwable cause)
    {
        super(cause);
    }

}
