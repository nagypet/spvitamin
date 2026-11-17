package hu.perit.spvitamin.spring.email.imap.service;

import hu.perit.spvitamin.spring.email.imap.auth.OAuth2TokenProvider;
import hu.perit.spvitamin.spring.email.imap.client.DefaultImapClient;
import hu.perit.spvitamin.spring.email.imap.client.ImapClientFactory;
import hu.perit.spvitamin.spring.email.imap.client.ImapStoreOperation;
import hu.perit.spvitamin.spring.email.imap.config.ImapDownloaderProperties;
import hu.perit.spvitamin.spring.email.imap.model.EmailDownloadResult;
import hu.perit.spvitamin.spring.email.imap.model.EmailMessage;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImapEmailDownloaderTest
{
    private ImapDownloaderProperties imapProps;
    private ImapClientFactory imapClientFactory;
    private ImapEmailDownloader imapEmailDownloader;

    private MockedConstruction<DefaultImapClient> mockedClientConstruction;


    @BeforeEach
    void setUp()
    {
        imapProps = new ImapDownloaderProperties();
        imapProps.setEnabled(true);
        imapProps.setAuthType(ImapDownloaderProperties.AuthType.BASIC);
        imapProps.setHost("imap.test");
        imapProps.setUsername("user@test");
        imapProps.setPassword("secret");
        imapProps.setFolder("INBOX");

        imapClientFactory = new ImapClientFactory(mock(OAuth2TokenProvider.class));
        imapEmailDownloader = new ImapEmailDownloaderImpl(imapClientFactory);
    }


    @AfterEach
    void tearDown()
    {
        if (mockedClientConstruction != null)
        {
            mockedClientConstruction.close();
        }
    }


    @Test
    void downloadEmails_returnsEmpty_whenDisabled()
    {
        imapProps.setEnabled(false);

        ImapEmailDownloader service = new ImapEmailDownloaderImpl(imapClientFactory);

        mockedClientConstruction = mockConstruction(DefaultImapClient.class);

        EmailDownloadResult result = service.downloadEmails(imapProps);

        assertThat(result).isNotNull();
        assertThat(result.eligible()).isEmpty();
        // No IMAP client should be constructed when disabled
        assertThat(mockedClientConstruction.constructed()).isEmpty();
    }


    @Test
    void downloadEmails_returnsMappedList_whenEnabled() throws Exception
    {
        // Arrange store/folder/message graph
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        Message message = mock(Message.class);

        when(store.getFolder("INBOX")).thenReturn(folder);
        // Folder interactions
        doNothing().when(folder).open(Folder.READ_ONLY);
        when(folder.getMessages()).thenReturn(new Message[]{message});

        // Message stubs
        when(message.getMessageNumber()).thenReturn(1);
        when(message.getSubject()).thenReturn("Subject");
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@test", "Sender Name")});
        when(message.getSentDate()).thenReturn(new Date());
        when(message.getContent()).thenReturn("Hello");

        // Mock construction of DefaultImapClient to delegate executeWithStore to our mock store
        mockedClientConstruction = mockConstruction(DefaultImapClient.class, (mock, context) ->
                when(mock.executeWithStore(any())).thenAnswer(invocation -> {
                    var op = invocation.getArgument(0);
                    // ImapStoreOperation<T>.execute(store)
                    return ((ImapStoreOperation<?>) op).execute(store);
                })
        );

        ImapEmailDownloader service = new ImapEmailDownloaderImpl(imapClientFactory);

        // Act
        List<EmailMessage> result = service.downloadEmails(imapProps).eligible();

        // Assert
        assertThat(result).hasSize(1);
        EmailMessage em = result.get(0);
        assertThat(em.getMessageId()).isEqualTo("1");
        assertThat(em.getSubject()).isEqualTo("Subject");
        assertThat(em.getSenderEmail()).isEqualTo("sender@test");
        assertThat(em.getSenderName()).isEqualTo("Sender Name");
        assertThat(em.getContent()).contains("Hello");
    }


    @Test
    void downloadEmails_returnsEmpty_whenFolderIsEmpty() throws Exception
    {
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);

        when(store.getFolder("INBOX")).thenReturn(folder);
        doNothing().when(folder).open(Folder.READ_ONLY);
        when(folder.getMessages()).thenReturn(new Message[]{});

        mockedClientConstruction = mockConstruction(DefaultImapClient.class, (mock, context) ->
                when(mock.executeWithStore(any())).thenAnswer(invocation -> {
                    var op = invocation.getArgument(0);
                    return ((ImapStoreOperation<?>) op).execute(store);
                })
        );

        EmailDownloadResult result = imapEmailDownloader.downloadEmails(imapProps);

        assertThat(result.eligible()).isEmpty();
        assertThat(result.unprocessable()).isEmpty();
    }


    @Test
    void downloadEmails_oversizedMessage_goesToOversizedList() throws Exception
    {
        imapProps.setMaxMessageSizeMb(1);

        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        Message message = mock(Message.class);

        when(store.getFolder("INBOX")).thenReturn(folder);
        doNothing().when(folder).open(Folder.READ_ONLY);
        when(folder.getMessages()).thenReturn(new Message[]{message});

        // 2 MB > 1 MB limit → oversized
        when(message.getSize()).thenReturn(2 * 1024 * 1024);
        when(message.getMessageNumber()).thenReturn(42);
        when(message.getSubject()).thenReturn("Big Email");
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("big@test", "Big Sender")});
        when(message.getSentDate()).thenReturn(new Date());

        mockedClientConstruction = mockConstruction(DefaultImapClient.class, (mock, context) ->
                when(mock.executeWithStore(any())).thenAnswer(invocation -> {
                    var op = invocation.getArgument(0);
                    return ((ImapStoreOperation<?>) op).execute(store);
                })
        );

        EmailDownloadResult result = imapEmailDownloader.downloadEmails(imapProps);

        assertThat(result.eligible()).isEmpty();
        assertThat(result.unprocessable()).hasSize(1);
        EmailMessage oversized = result.unprocessable().get(0);
        assertThat(oversized.getMessageId()).isEqualTo("42");
        assertThat(oversized.getSubject()).isEqualTo("Big Email");
        assertThat(oversized.getSenderEmail()).isEqualTo("big@test");
        assertThat(oversized.getSenderName()).isEqualTo("Big Sender");
    }


    @Test
    void downloadEmails_mixedSizeMessages_splitIntoEligibleAndOversized() throws Exception
    {
        imapProps.setMaxMessageSizeMb(1);

        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        Message smallMsg = mock(Message.class);
        Message bigMsg = mock(Message.class);

        when(store.getFolder("INBOX")).thenReturn(folder);
        doNothing().when(folder).open(Folder.READ_ONLY);
        when(folder.getMessages()).thenReturn(new Message[]{smallMsg, bigMsg});

        // Small message (eligible): 100 KB < 1 MB limit
        when(smallMsg.getSize()).thenReturn(100 * 1024);
        when(smallMsg.getMessageNumber()).thenReturn(1);
        when(smallMsg.getSubject()).thenReturn("Small Email");
        when(smallMsg.getFrom()).thenReturn(new Address[]{new InternetAddress("small@test", "Small Sender")});
        when(smallMsg.getSentDate()).thenReturn(new Date());
        when(smallMsg.getContent()).thenReturn("Hello small");

        // Big message (oversized): 2 MB > 1 MB limit
        when(bigMsg.getSize()).thenReturn(2 * 1024 * 1024);
        when(bigMsg.getMessageNumber()).thenReturn(2);
        when(bigMsg.getSubject()).thenReturn("Big Email");
        when(bigMsg.getFrom()).thenReturn(new Address[]{new InternetAddress("big@test", "Big Sender")});
        when(bigMsg.getSentDate()).thenReturn(new Date());

        mockedClientConstruction = mockConstruction(DefaultImapClient.class, (mock, context) ->
                when(mock.executeWithStore(any())).thenAnswer(invocation -> {
                    var op = invocation.getArgument(0);
                    return ((ImapStoreOperation<?>) op).execute(store);
                })
        );

        EmailDownloadResult result = imapEmailDownloader.downloadEmails(imapProps);

        assertThat(result.eligible()).hasSize(1);
        assertThat(result.eligible().get(0).getMessageId()).isEqualTo("1");
        assertThat(result.unprocessable()).hasSize(1);
        EmailMessage oversized = result.unprocessable().get(0);
        assertThat(oversized.getMessageId()).isEqualTo("2");
        assertThat(oversized.getSubject()).isEqualTo("Big Email");
        assertThat(oversized.getSenderEmail()).isEqualTo("big@test");
    }


    @Test
    void downloadEmails_returnsEmpty_whenExecutionThrows()
    {
        mockedClientConstruction = mockConstruction(DefaultImapClient.class, (mock, context) ->
                when(mock.executeWithStore(any())).thenThrow(new RuntimeException("IMAP connection failed"))
        );

        EmailDownloadResult result = imapEmailDownloader.downloadEmails(imapProps);

        assertThat(result.eligible()).isEmpty();
        assertThat(result.unprocessable()).isEmpty();
    }


    @Test
    void getFolders_returnsFolderNames_whenEnabled() throws Exception
    {
        Store store = mock(Store.class);
        Folder defaultFolder = mock(Folder.class);
        Folder f1 = mock(Folder.class);
        Folder f2 = mock(Folder.class);

        when(store.getDefaultFolder()).thenReturn(defaultFolder);
        when(defaultFolder.list()).thenReturn(new Folder[]{f1, f2});
        when(f1.getFullName()).thenReturn("INBOX");
        when(f2.getFullName()).thenReturn("Archive");

        mockedClientConstruction = mockConstruction(DefaultImapClient.class, (mock, context) ->
                when(mock.executeWithStore(any())).thenAnswer(invocation -> {
                    var op = invocation.getArgument(0);
                    return ((ImapStoreOperation<?>) op).execute(store);
                })
        );

        List<String> folders = imapEmailDownloader.getFolders(imapProps);

        assertThat(folders).containsExactlyInAnyOrder("INBOX", "Archive");
    }


    @Test
    void deleteEmail_returnsTrue_whenMessageFound() throws Exception
    {
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        Message m1 = mock(Message.class);
        Message m2 = mock(Message.class);

        when(store.getFolder("INBOX")).thenReturn(folder);
        doNothing().when(folder).open(Folder.READ_WRITE);
        when(folder.getMessages()).thenReturn(new Message[]{m1, m2});

        when(m1.getMessageNumber()).thenReturn(10);
        when(m2.getMessageNumber()).thenReturn(5);

        mockedClientConstruction = mockConstruction(DefaultImapClient.class, (mock, context) ->
                when(mock.executeWithStore(any())).thenAnswer(invocation -> {
                    var op = invocation.getArgument(0);
                    return ((ImapStoreOperation<?>) op).execute(store);
                })
        );

        int deleted = imapEmailDownloader.deleteEmails(imapProps, List.of("5"));
        assertThat(deleted).isEqualTo(1);
        verify(m2, times(1)).setFlag(Flags.Flag.DELETED, true);
    }


    @Test
    void deleteEmail_returnsFalse_whenNotFound() throws Exception
    {
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        Message m1 = mock(Message.class);

        when(store.getFolder("INBOX")).thenReturn(folder);
        doNothing().when(folder).open(Folder.READ_WRITE);
        when(folder.getMessages()).thenReturn(new Message[]{m1});
        when(m1.getMessageNumber()).thenReturn(7);

        mockedClientConstruction = mockConstruction(DefaultImapClient.class, (mock, context) ->
                when(mock.executeWithStore(any())).thenAnswer(invocation -> {
                    var op = invocation.getArgument(0);
                    return ((ImapStoreOperation<?>) op).execute(store);
                })
        );

        int deleted = imapEmailDownloader.deleteEmails(imapProps, List.of("5"));

        assertThat(deleted).isZero();
        verify(m1, never()).setFlag(any(), anyBoolean());
    }


    @Test
    void deleteEmail_returnsFalse_whenDisabled()
    {
        imapProps.setEnabled(false);

        mockedClientConstruction = mockConstruction(DefaultImapClient.class);

        int deleted = imapEmailDownloader.deleteEmails(imapProps, List.of("123"));

        assertThat(deleted).isZero();
        assertThat(mockedClientConstruction.constructed()).isEmpty();
    }
}
