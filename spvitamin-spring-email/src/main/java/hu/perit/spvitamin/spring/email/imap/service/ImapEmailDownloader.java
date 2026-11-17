package hu.perit.spvitamin.spring.email.imap.service;

import hu.perit.spvitamin.spring.email.imap.config.ImapDownloaderProperties;
import hu.perit.spvitamin.spring.email.imap.model.EmailDownloadResult;

import java.util.Collection;
import java.util.List;

public interface ImapEmailDownloader
{
    List<String> getFolders(ImapDownloaderProperties props);

    EmailDownloadResult downloadEmails(ImapDownloaderProperties props);

    int deleteEmails(ImapDownloaderProperties props, Collection<String> messageIds);

    void moveEmailsToFolder(ImapDownloaderProperties props, Collection<String> messageIds, String folderName);
}
