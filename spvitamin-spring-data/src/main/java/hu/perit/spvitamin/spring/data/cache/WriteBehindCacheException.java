package hu.perit.spvitamin.spring.data.cache;

public class WriteBehindCacheException extends RuntimeException
{

    public WriteBehindCacheException(String message)
    {
        super(message);
    }

    public WriteBehindCacheException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
