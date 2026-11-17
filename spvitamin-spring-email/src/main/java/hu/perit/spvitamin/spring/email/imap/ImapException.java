package hu.perit.spvitamin.spring.email.imap;

import java.io.Serial;

public class ImapException extends RuntimeException
{
    @Serial
    private static final long serialVersionUID = -6180666037638256529L;


    public ImapException(String message)
    {
        super(message);
    }


    public ImapException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
