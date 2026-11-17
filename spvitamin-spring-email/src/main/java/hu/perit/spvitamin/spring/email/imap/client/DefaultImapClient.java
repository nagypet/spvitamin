package hu.perit.spvitamin.spring.email.imap.client;


import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.email.imap.config.ImapConnectionConfig;
import hu.perit.spvitamin.spring.email.imap.ImapException;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default implementation of the ImapClient interface.
 * This class provides functionality for connecting to an IMAP server, executing operations on the IMAP store,
 * and managing the connection lifecycle. It uses Jakarta Mail API for IMAP operations and implements thread-safety
 * using a ReentrantLock to ensure that concurrent operations on the same IMAP connection are properly synchronized.
 */
@Slf4j
public class DefaultImapClient implements ImapClient
{
    private final ReentrantLock lock = new ReentrantLock();
    private Store store;


    public DefaultImapClient(ImapConnectionConfig config)
    {
        connect(config);
    }


    @Override
    public void connect(ImapConnectionConfig config)
    {
        Properties props = new Properties();
        if (config.getAdditionalProperties() != null && !config.getAdditionalProperties().isEmpty())
        {
            props.putAll(config.getAdditionalProperties());
        }

        if (config.isUseSSL())
        {
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.port", String.valueOf(config.getPort()));
        }
        props.put("mail.store.protocol", "imap");

        lock.lock();
        try
        {
            Session session = Session.getInstance(props);
            store = session.getStore("imap");
            store.connect(config.getHost(), config.getPort(), config.getUsername(), config.getPassword());
        }
        catch (AuthenticationFailedException e)
        {
            throw new ImapException(String.format("Failed to connect to '%s:%d' with '%s'",
                    config.getHost(), config.getPort(), config.getUsername()), e);
        }
        catch (MessagingException e)
        {
            throw new ImapException("Failed to establish IMAP connection", e);
        }
        finally
        {
            lock.unlock();
        }
    }


    @Override
    public <T> T executeWithStore(ImapStoreOperation<T> operation)
    {
        lock.lock();
        try
        {
            if (store == null || !store.isConnected())
            {
                throw new ImapException("IMAP store is not connected!");
            }
            return operation.execute(store);
        }
        catch (MessagingException | RuntimeException e)
        {
            throw new ImapException("IMAP operation failed!", e);
        }
        finally
        {
            lock.unlock();
        }
    }


    @Override
    public void close()
    {
        lock.lock();
        try
        {
            if (store != null && store.isConnected())
            {
                try
                {
                    store.close();
                }
                catch (MessagingException e)
                {
                    log.warn(StackTracer.toString(e));
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }
}
