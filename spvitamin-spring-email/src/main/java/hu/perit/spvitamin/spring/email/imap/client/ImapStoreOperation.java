package hu.perit.spvitamin.spring.email.imap.client;

import jakarta.mail.MessagingException;
import jakarta.mail.Store;

/**
 * Functional interface for executing operations on an IMAP store.
 * Implementations of this interface define specific operations to be performed on the IMAP store.
 * 
 * @param <T> The return type of the operation executed on the IMAP store
 */
public interface ImapStoreOperation<T>
{
    T execute(Store store) throws MessagingException;
}
