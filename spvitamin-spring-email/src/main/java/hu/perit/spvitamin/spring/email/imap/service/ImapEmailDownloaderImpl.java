package hu.perit.spvitamin.spring.email.imap.service;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.email.imap.client.ImapClient;
import hu.perit.spvitamin.spring.email.imap.client.ImapClientFactory;
import hu.perit.spvitamin.spring.email.imap.config.ImapDownloaderProperties;
import hu.perit.spvitamin.spring.email.imap.model.EmailDownloadResult;
import hu.perit.spvitamin.spring.email.imap.model.EmailMessage;
import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import jakarta.mail.search.FlagTerm;

/**
 * Általános IMAP letöltő algoritmus.
 * Letölti a konfigurált mappa összes levelét, méret szerint szétválogatja őket
 * eligible (teljes tartalom) és oversized (csak metaadat) csoportokba.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ImapEmailDownloaderImpl implements ImapEmailDownloader
{
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(5);
    private static final int DELETE_RETRY_COUNT = 3;
    private static final Duration DELETE_RETRY_DELAY = Duration.ofSeconds(2);

    private final ImapClientFactory imapClientFactory;

    /**
     * Metódus-szintű watchdog timeout-hoz.
     */
    private final ExecutorService imapExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("imap-download-", 0).factory());

    /**
     * Runtime limit-csökkentés timeout esetén. Kulcs: host:port:username:folder.
     * Ha timeout jön, felezzük a limitet (min. 1); sikeres letöltésnél visszaállítjuk az eredetire.
     * Ha limit=1 esetén is timeout jön, a problémás üzenetet áthelyezzük a Quarantine mappába.
     */
    private final ConcurrentHashMap<String, Integer> effectiveLimits = new ConcurrentHashMap<>();


    @Override
    public List<String> getFolders(ImapDownloaderProperties props)
    {
        if (!props.isEnabled())
        {
            return List.of();
        }

        // Always get a fresh config
        try (ImapClient imapClient = this.imapClientFactory.createClient(props))
        {
            return imapClient.executeWithStore(store -> {
                Folder defaultFolder = store.getDefaultFolder();
                return Arrays.stream(defaultFolder.list())
                        .map(Folder::getFullName)
                        .toList();
            });
        }
    }


    /**
     * Letölti az összes levelet a {@code props.getFolder()} mappából.
     *
     * @param props a letöltési konfiguráció (mappa neve, max méret)
     * @return az eligible és oversized levelek listája
     */
    @Override
    public EmailDownloadResult downloadEmails(ImapDownloaderProperties props)
    {
        if (!props.isEnabled())
        {
            return new EmailDownloadResult(List.of(), List.of(), 0);
        }

        // Important for testability: construct the client on the caller thread so that
        // construction-mocking can intercept it reliably, even if the task runs on another thread.
        final ImapClient imapClient = this.imapClientFactory.createClient(props);

        String accountKey = buildAccountKey(props);
        int configuredLimit = props.getMaxMessagesPerRun();
        int effectiveLimit = effectiveLimits.getOrDefault(accountKey, configuredLimit);

        // Capture ThreadContext (MDC) here on the caller thread; virtual threads don't inherit it.
        Map<String, String> context = ThreadContext.getContext();
        Callable<EmailDownloadResult> task = () -> {
            ThreadContext.putAll(context);
            try
            {
                return internalDownloadEmails(imapClient, props, effectiveLimit);
            }
            finally
            {
                ThreadContext.clearAll();
            }
        };
        Future<EmailDownloadResult> future = imapExecutor.submit(task);
        try
        {
            EmailDownloadResult result = future.get(DOWNLOAD_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            // Sikeres letöltés: ha volt limit-csökkentés, visszaállítjuk az eredeti értékre.
            if (effectiveLimits.containsKey(accountKey))
            {
                effectiveLimits.remove(accountKey);
                log.info("IMAP: download succeeded, restoring maxMessagesPerRun to {} for {}", configuredLimit, accountKey);
            }
            return result;
        }
        catch (TimeoutException te)
        {
            // Megpróbáljuk megszakítani. Fontos: a cancel(true) csak akkor hat,
            // ha a stack reagál interrupt-ra; ezért van mellette a mail timeout is.
            future.cancel(true);
            if (effectiveLimit <= 1)
            {
                // Ha limit=1 esetén is timeout jön, az első üzenet a "méreg" → Quarantine mappába tesszük.
                log.warn("IMAP timeout at maxMessagesPerRun=1 for {}; quarantining first message to break the loop.", accountKey);
                quarantineFirstMessage(props);
                effectiveLimits.remove(accountKey);
            }
            else
            {
                int halved = Math.max(1, effectiveLimit / 2);
                effectiveLimits.put(accountKey, halved);
                log.warn("IMAP timeout after {}: reducing maxMessagesPerRun from {} to {} for {}",
                        DOWNLOAD_TIMEOUT, effectiveLimit, halved, accountKey);
            }
            return new EmailDownloadResult(List.of(), List.of(), 0);
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return new EmailDownloadResult(List.of(), List.of(), 0);
        }
        catch (ExecutionException ee)
        {
            // Az eredeti exception itt van a cause-ban
            Throwable cause = ee.getCause() != null ? ee.getCause() : ee;
            log.error("IMAP downloadEmails() failed: {}", StackTracer.toString(cause));
            return new EmailDownloadResult(List.of(), List.of(), 0);
        }
        finally
        {
            try
            {
                imapClient.close();
            }
            catch (Exception ignored)
            {
                // already logged inside client
            }
        }
    }


    EmailDownloadResult internalDownloadEmails(ImapClient client, ImapDownloaderProperties props, int effectiveLimit)
    {
        return client.executeWithStore(store -> {
            log.trace("IMAP: getting folder reference...");
            try (Folder folder = store.getFolder(props.getFolder()))
            {
                log.trace("IMAP: opening folder...");
                folder.open(Folder.READ_ONLY);
                log.trace("IMAP: folder opened, fetching message list...");
                // Use a server-side SEARCH to exclude \Deleted-flagged messages.
                // O365/Exchange IMAP may retain soft-deleted messages (marked \Deleted but
                // not yet expunged) in the folder view across sessions. A client-side filter
                // via isSet() would trigger one FETCH per message — far too expensive for
                // large inboxes. The SEARCH NOT DELETED command is handled entirely by the
                // server and returns only the messages we actually want to process.
                Message[] allMessages = folder.search(new FlagTerm(new Flags(Flags.Flag.DELETED), false));
                if (allMessages.length == 0)
                {
                    return new EmailDownloadResult(List.of(), List.of(), 0);
                }

                // Limit the number of messages processed per run to avoid timeouts on large backlogs.
                // effectiveLimit may be reduced from the configured value by the timeout-halving mechanism.
                int limit = effectiveLimit;
                Message[] messages = (limit > 0 && allMessages.length > limit)
                        ? Arrays.copyOf(allMessages, limit)
                        : allMessages;
                int remainingCount = allMessages.length - messages.length;
                if (remainingCount > 0)
                {
                    log.info("IMAP: inbox has {} messages, processing first {} (maxMessagesPerRun limit); {} remain for subsequent runs.",
                            allMessages.length, messages.length, remainingCount);
                }
                else
                {
                    log.info("IMAP: got {} messages, fetching metadata...", messages.length);
                }

                folder.fetch(messages, getMetaProfile());

                int maxSizeBytes = props.getMaxMessageSizeMb() * 1024 * 1024;
                int maxSizeMb = props.getMaxMessageSizeMb();
                long maxBatchSizeBytes = (long) props.getMaxBatchSizeMb() * 1024 * 1024;
                List<EmailMessage> unprocessableList = new ArrayList<>();
                List<Message> eligibleList = new ArrayList<>();
                long cumulativeSizeBytes = 0;
                for (int i = 0; i < messages.length; i++)
                {
                    Message message = messages[i];
                    try
                    {
                        int size = getMessageSize(message);
                        if (size > maxSizeBytes)
                        {
                            log.warn("IMAP: skipping message '{}' from {} — size {} MB exceeds limit of {} MB",
                                    message.getSubject(), message.getFrom(), size / 1024 / 1024, maxSizeMb);
                            unprocessableList.add(EmailMessage.withMetadata(message));
                        }
                        else if (maxBatchSizeBytes > 0 && cumulativeSizeBytes + size > maxBatchSizeBytes)
                        {
                            int deferredCount = messages.length - i;
                            remainingCount += deferredCount;
                            log.info("IMAP: batch size limit reached ({} MB); deferring {} message(s) to next run.",
                                    props.getMaxBatchSizeMb(), deferredCount);
                            break;
                        }
                        else
                        {
                            eligibleList.add(message);
                            cumulativeSizeBytes += size;
                        }
                    }
                    catch (RuntimeException e)
                    {
                        log.error("IMAP: could not extract envelope from message: {}, {}", message.getMessageNumber(), e.getMessage());
                    }
                }

                Message[] eligible = eligibleList.toArray(new Message[0]);
                log.info("IMAP: {} eligible, {} unprocessable; bulk-fetching bodies...", eligible.length, unprocessableList.size());
                folder.fetch(eligible, getBodyProfile());
                log.trace("IMAP: prefetch done, processing...");

                // Processing eligible messages
                List<EmailMessage> emailMessages = Arrays.stream(eligible)
                        .map(message -> {
                            EmailMessage emailMessage = EmailMessage.withMetadataAndContent(message);
                            if (emailMessage.isValid())
                            {
                                return emailMessage;
                            }
                            unprocessableList.add(emailMessage);
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .toList();
                return new EmailDownloadResult(emailMessages, unprocessableList, remainingCount);
            }
        });
    }


    private static FetchProfile getMetaProfile()
    {
        FetchProfile metaProfile = new FetchProfile();
        metaProfile.add(FetchProfile.Item.ENVELOPE);
        metaProfile.add(FetchProfile.Item.CONTENT_INFO);
        return metaProfile;
    }


    private static FetchProfile getBodyProfile()
    {
        FetchProfile bodyProfile = new FetchProfile();
        bodyProfile.add(IMAPFolder.FetchProfileItem.MESSAGE);
        return bodyProfile;
    }


    static int getMessageSize(Message m)
    {
        try
        {
            return m.getSize();
        }
        catch (MessagingException e)
        {
            return 0;
        }
    }


    @Override
    public int deleteEmails(ImapDownloaderProperties props, Collection<String> messageIds)
    {
        if (!props.isEnabled() || messageIds == null || messageIds.isEmpty())
        {
            return 0;
        }

        return withRetry(
                () -> deleteEmailsOnce(props, messageIds),
                this::isTransientImapFailure,
                DELETE_RETRY_COUNT,
                DELETE_RETRY_DELAY
        );
    }


    @Override
    public void moveEmailsToFolder(ImapDownloaderProperties props, Collection<String> messageIds, String folderName)
    {
        if (!props.isEnabled() || messageIds == null || messageIds.isEmpty())
        {
            return;
        }

        try (ImapClient imapClient = this.imapClientFactory.createClient(props))
        {
            imapClient.executeWithStore(store -> {
                Folder unprocessedFolder = store.getFolder(folderName);
                if (!unprocessedFolder.exists())
                {
                    unprocessedFolder.create(Folder.HOLDS_MESSAGES);
                    log.info("Created IMAP folder: {}", folderName);
                }

                try (Folder sourceFolder = store.getFolder(props.getFolder()))
                {
                    sourceFolder.open(Folder.READ_WRITE);

                    List<Message> toMove = Arrays.stream(sourceFolder.getMessages())
                            .filter(m -> messageIds.contains(String.valueOf(m.getMessageNumber())))
                            .toList();

                    if (!toMove.isEmpty())
                    {
                        Message[] messageArray = toMove.toArray(new Message[0]);
                        sourceFolder.copyMessages(messageArray, unprocessedFolder);
                        for (Message m : toMove)
                        {
                            m.setFlag(Flags.Flag.DELETED, true);
                        }
                        sourceFolder.expunge();
                        List<String> movedIds = toMove.stream().map(m -> String.valueOf(m.getMessageNumber())).toList();
                        log.info("Moved {} email(s) from folder {} to folder {} with message numbers: {}", toMove.size(), props.getFolder(), folderName, movedIds);
                    }
                }

                return null;
            });
        }
    }


    int deleteEmailsOnce(ImapDownloaderProperties props, Collection<String> messageIds)
    {
        log.debug("Deleting {} email(s) from folder {} with IDs: {}", messageIds.size(), props.getFolder(), messageIds);
        try (ImapClient imapClient = this.imapClientFactory.createClient(props))
        {
            return imapClient.executeWithStore(store -> {
                int deletedCount = 0;

                try (Folder folder = store.getFolder(props.getFolder()))
                {
                    folder.open(Folder.READ_WRITE);

                    Message[] messages = folder.getMessages();
                    List<String> deletedMessages = new ArrayList<>();
                    for (Message message : messages)
                    {
                        String currentMessageId = String.valueOf(message.getMessageNumber());
                        if (messageIds.contains(currentMessageId))
                        {
                            deletedMessages.add(currentMessageId);
                            message.setFlag(Flags.Flag.DELETED, true);
                            deletedCount++;
                        }
                    }

                    folder.expunge();
                    log.info("Deleted {} email(s) from folder {} with IDs: {}", deletedMessages.size(), props.getFolder(), deletedMessages);
                }

                return deletedCount;
            });
        }
    }


    <T> T withRetry(RetryableOperation<T> operation,
                    Predicate<Throwable> retryPredicate,
                    int maxAttempts,
                    Duration delay)
    {
        int attempt = 0;
        while (true)
        {
            try
            {
                return operation.execute();
            }
            catch (RuntimeException e)
            {
                attempt++;

                if (attempt >= maxAttempts || !retryPredicate.test(e))
                {
                    throw e;
                }

                log.warn("Operation failed on attempt {}/{}. Retrying...", attempt, maxAttempts, e);
                sleepQuietly(delay);
            }
        }
    }


    private boolean isTransientImapFailure(Throwable throwable)
    {
        Throwable current = throwable;
        while (current != null)
        {
            if (current instanceof SocketTimeoutException)
            {
                return true;
            }

            if (current instanceof MessagingException)
            {
                String message = current.getMessage();
                if (message != null && (
                        message.contains("not connected") ||
                                message.contains("Read timed out") ||
                                message.contains("BYE") ||
                                message.contains("ConnectionException")
                ))
                {
                    return true;
                }
            }

            current = current.getCause();
        }
        return false;
    }


    private static String buildAccountKey(ImapDownloaderProperties props)
    {
        return props.getHost() + ":" + props.getPort() + ":" + props.getUsername() + ":" + props.getFolder();
    }


    private void quarantineFirstMessage(ImapDownloaderProperties props)
    {
        try (ImapClient imapClient = this.imapClientFactory.createClient(props))
        {
            String firstMessageId = imapClient.executeWithStore(store -> {
                try (Folder folder = store.getFolder(props.getFolder()))
                {
                    folder.open(Folder.READ_ONLY);
                    Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.DELETED), false));
                    if (messages.length == 0)
                    {
                        return null;
                    }
                    Message first = messages[0];
                    log.warn("IMAP quarantine: moving message {} (subject: '{}') from {} to Quarantine",
                            first.getMessageNumber(), safeSubject(first), props.getFolder());
                    return String.valueOf(first.getMessageNumber());
                }
            });

            if (firstMessageId == null)
            {
                log.warn("IMAP quarantine: no messages found in folder {}", props.getFolder());
                return;
            }

            moveEmailsToFolder(props, List.of(firstMessageId), "Quarantine");
        }
        catch (Exception e)
        {
            log.error("IMAP quarantine failed: {}", StackTracer.toString(e));
        }
    }


    private static String safeSubject(Message message)
    {
        try
        {
            return message.getSubject();
        }
        catch (MessagingException e)
        {
            return "<unknown>";
        }
    }


    private void sleepQuietly(Duration duration)
    {
        try
        {
            Thread.sleep(duration.toMillis());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }
}
