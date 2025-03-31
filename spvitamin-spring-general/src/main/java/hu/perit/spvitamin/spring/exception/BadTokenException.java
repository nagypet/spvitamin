package hu.perit.spvitamin.spring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, code = HttpStatus.BAD_REQUEST)
public class BadTokenException extends RuntimeException
{
    @Serial
    private static final long serialVersionUID = -6596296814497856887L;

    public BadTokenException(String message)
    {
        super(message);
    }

    public BadTokenException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BadTokenException(Throwable cause)
    {
        super(cause);
    }
}
