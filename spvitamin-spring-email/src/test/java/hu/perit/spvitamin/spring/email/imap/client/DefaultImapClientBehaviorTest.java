package hu.perit.spvitamin.spring.email.imap.client;

import hu.perit.spvitamin.spring.email.imap.ImapException;
import hu.perit.spvitamin.spring.email.imap.config.ImapConnectionConfig;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultImapClientBehaviorTest
{
    private MockedStatic<Session> sessionStaticMock;
    private Session session;
    private Store store;

    @BeforeEach
    void setUp() throws Exception
    {
        session = mock(Session.class);
        store = mock(Store.class);

        sessionStaticMock = mockStatic(Session.class);
        sessionStaticMock.when(() -> Session.getInstance(any(Properties.class))).thenReturn(session);

        when(session.getStore("imap")).thenReturn(store);
        doNothing().when(store).connect(any(), anyInt(), any(), any());
        when(store.isConnected()).thenReturn(true);
    }

    @AfterEach
    void tearDown()
    {
        if (sessionStaticMock != null)
        {
            sessionStaticMock.close();
        }
    }

    private static ImapConnectionConfig baseConfig(boolean ssl)
    {
        return ImapConnectionConfig.builder()
                .host("imap.test")
                .port(993)
                .username("user@test")
                .password("secret")
                .useSSL(ssl)
                .build();
    }

    @Test
    void connect_withSSL_setsProperties_and_connects() throws Exception
    {
        AtomicReference<Properties> capturedProps = new AtomicReference<>();
        sessionStaticMock.when(() -> Session.getInstance(any(Properties.class))).thenAnswer(inv -> {
            capturedProps.set(inv.getArgument(0));
            return session;
        });

        ImapConnectionConfig cfg = baseConfig(true);
        try (DefaultImapClient client = new DefaultImapClient(cfg))
        {
            verify(session).getStore("imap");
            verify(store).connect("imap.test", 993, "user@test", "secret");

            Properties p = capturedProps.get();
            assertThat(p).isNotNull();
            assertThat(p.getProperty("mail.store.protocol")).isEqualTo("imap");
            assertThat(p.getProperty("mail.imap.ssl.enable")).isEqualTo("true");
            assertThat(p.getProperty("mail.imap.port")).isEqualTo("993");
        }
    }

    @Test
    void connect_withoutSSL_setsProtocol_only()
    {
        AtomicReference<Properties> capturedProps = new AtomicReference<>();
        sessionStaticMock.when(() -> Session.getInstance(any(Properties.class))).thenAnswer(inv -> {
            capturedProps.set(inv.getArgument(0));
            return session;
        });

        ImapConnectionConfig cfg = baseConfig(false);
        try (DefaultImapClient ignored = new DefaultImapClient(cfg))
        {
            Properties p = capturedProps.get();
            assertThat(p).isNotNull();
            assertThat(p.getProperty("mail.store.protocol")).isEqualTo("imap");
            assertThat(p.getProperty("mail.imap.ssl.enable")).isNull();
            assertThat(p.getProperty("mail.imap.port")).isNull();
        }
    }

    @Test
    void connect_merges_additionalProperties()
    {
        AtomicReference<Properties> capturedProps = new AtomicReference<>();
        sessionStaticMock.when(() -> Session.getInstance(any(Properties.class))).thenAnswer(inv -> {
            capturedProps.set(inv.getArgument(0));
            return session;
        });

        Properties extra = new Properties();
        extra.setProperty("mail.imap.sasl.enable", "true");
        extra.setProperty("custom.key", "value");

        ImapConnectionConfig cfg = ImapConnectionConfig.builder()
                .host("imap.test")
                .port(993)
                .username("user@test")
                .password("token")
                .useSSL(true)
                .additionalProperties(extra)
                .build();

        try (DefaultImapClient ignored = new DefaultImapClient(cfg))
        {
            Properties p = capturedProps.get();
            assertThat(p.getProperty("mail.imap.sasl.enable")).isEqualTo("true");
            assertThat(p.getProperty("custom.key")).isEqualTo("value");
        }
    }

    @Test
    void connect_whenAuthFails_throwsImapException() throws Exception
    {
        doThrow(new AuthenticationFailedException("bad creds")).when(store)
                .connect(any(), anyInt(), any(), any());

        ImapConnectionConfig cfg = baseConfig(true);
        assertThatThrownBy(() -> new DefaultImapClient(cfg))
                .isInstanceOf(ImapException.class)
                .hasMessageContaining("Failed to connect to")
                .hasMessageContaining("imap.test")
                .hasMessageContaining("user@test");
    }

    @Test
    void connect_whenMessagingFails_throwsImapException() throws Exception
    {
        // Simulate MessagingException during connect phase
        doThrow(new MessagingException("store error")).when(store)
                .connect(any(), anyInt(), any(), any());
        ImapConnectionConfig cfg = baseConfig(true);
        assertThatThrownBy(() -> new DefaultImapClient(cfg))
                .isInstanceOf(ImapException.class)
                .hasMessageContaining("Failed to establish IMAP connection");
    }

    @Test
    void executeWithStore_whenNotConnected_throwsImapException()
    {
        ImapConnectionConfig cfg = baseConfig(true);
        try (DefaultImapClient client = new DefaultImapClient(cfg))
        {
            when(store.isConnected()).thenReturn(false);
            assertThatThrownBy(() -> client.executeWithStore(s -> null))
                    .isInstanceOf(ImapException.class)
                    .hasMessageContaining("IMAP operation failed");
        }
    }

    @Test
    void executeWithStore_wrapsMessagingException()
    {
        ImapConnectionConfig cfg = baseConfig(true);
        try (DefaultImapClient client = new DefaultImapClient(cfg))
        {
            when(store.isConnected()).thenReturn(true);
            assertThatThrownBy(() -> client.executeWithStore(s -> { throw new MessagingException("op fail"); }))
                    .isInstanceOf(ImapException.class)
                    .hasMessageContaining("IMAP operation failed");
        }
    }

    @Test
    void close_whenConnected_closesStore() throws Exception
    {
        ImapConnectionConfig cfg = baseConfig(true);
        DefaultImapClient client = new DefaultImapClient(cfg);
        client.close();
        verify(store, times(1)).close();
    }

    @Test
    void close_whenCloseThrows_logsAndDoesNotPropagate() throws Exception
    {
        doThrow(new MessagingException("close fail")).when(store).close();
        ImapConnectionConfig cfg = baseConfig(true);
        DefaultImapClient client = new DefaultImapClient(cfg);
        // Should not throw
        client.close();
        verify(store, times(1)).close();
    }
}
