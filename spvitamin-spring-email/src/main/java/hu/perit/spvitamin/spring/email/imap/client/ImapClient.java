package hu.perit.spvitamin.spring.email.imap.client;

import hu.perit.spvitamin.spring.email.imap.config.ImapConnectionConfig;

/**
 * Interface for IMAP client operations.
 * Provides methods for connecting to an IMAP server, executing operations on the IMAP store,
 * and closing the connection when finished.
 * Implements AutoCloseable to ensure proper resource cleanup.
 */
public interface ImapClient extends AutoCloseable
{
    void connect(ImapConnectionConfig config);

    <T> T executeWithStore(ImapStoreOperation<T> operation);

    void close();
}
