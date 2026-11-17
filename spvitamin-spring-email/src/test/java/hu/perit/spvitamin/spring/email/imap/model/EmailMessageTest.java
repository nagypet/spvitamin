package hu.perit.spvitamin.spring.email.imap.model;

import hu.perit.spvitamin.json.typehelpers.OffsetDateTimeUtils;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailMessageTest
{
    @Test
    void fromJakartaMessage_withSimpleStringContent_noAttachments() throws Exception
    {
        // given
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(123);
        when(message.getSubject()).thenReturn("Hello Subject");
        Date sent = new Date();
        when(message.getSentDate()).thenReturn(sent);
        when(message.getContent()).thenReturn("Hello Body");
        Address[] from = {new InternetAddress("User Name <USER@Example.COM>")};
        when(message.getFrom()).thenReturn(from);

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getMessageId()).isEqualTo("123");
        assertThat(em.getSubject()).isEqualTo("Hello Subject");
        assertThat(em.getSenderEmail()).isEqualTo("user@example.com"); // lower-cased
        assertThat(em.getSenderName()).isEqualTo("User Name");
        assertThat(em.getSentDate()).isEqualTo(OffsetDateTimeUtils.fromDate(sent));
        assertThat(em.getContent()).isEqualTo("Hello Body");
        assertThat(em.getAttachments()).isEmpty();
    }


    @Test
    void fromJakartaMessage_withMultipart_text_and_attachments() throws Exception
    {
        // given
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(5);
        when(message.getSubject()).thenReturn("Multi");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@example.com", "Sender")});

        Multipart mp = mock(Multipart.class);
        when(message.getContent()).thenReturn(mp);

        BodyPart textPart = mock(BodyPart.class);
        when(textPart.getContentType()).thenReturn("text/plain; charset=utf-8");
        when(textPart.getContent()).thenReturn("Text1");
        when(textPart.getDisposition()).thenReturn(null);

        BodyPart attach1 = mock(BodyPart.class);
        when(attach1.getFileName()).thenReturn("file.txt");
        // mark as non-text to ensure it's treated as an attachment only
        when(attach1.getContentType()).thenReturn("application/octet-stream");
        when(attach1.getDisposition()).thenReturn(Part.ATTACHMENT);
        when(attach1.getInputStream()).thenReturn(new ByteArrayInputStream("ATTACHMENT-1".getBytes(StandardCharsets.UTF_8)));

        BodyPart attach2 = mock(BodyPart.class);
        when(attach2.getFileName()).thenReturn("image.png");
        when(attach2.getContentType()).thenReturn("image/png");
        when(attach2.getDisposition()).thenReturn(Part.INLINE);
        when(attach2.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3, 4}));

        when(mp.getCount()).thenReturn(3);
        when(mp.getBodyPart(0)).thenReturn(textPart);
        when(mp.getBodyPart(1)).thenReturn(attach1);
        when(mp.getBodyPart(2)).thenReturn(attach2);

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getContent()).contains("Text1");
        assertThat(em.getAttachments()).hasSize(2);
        assertThat(em.getAttachments().get(0).getFilename()).isEqualTo("file.txt");
        assertThat(em.getAttachments().get(0).getContentType()).isEqualTo("application/octet-stream");
        assertThat(new String(em.getAttachments().get(0).getData(), StandardCharsets.UTF_8)).isEqualTo("ATTACHMENT-1");
        assertThat(em.getAttachments().get(1).getFilename()).isEqualTo("image.png");
        assertThat(em.getAttachments().get(1).getContentType()).isEqualTo("image/png");
        assertThat(em.getAttachments().get(1).getData()).containsExactly(1, 2, 3, 4);
    }


    @Test
    void fromJakartaMessage_nestedMultipart_and_nonTextStringPart_included() throws Exception
    {
        // given
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(6);
        when(message.getSubject()).thenReturn("Nested");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("nester@example.com", "Nester")});

        Multipart outer = mock(Multipart.class);
        Multipart inner = mock(Multipart.class);
        when(message.getContent()).thenReturn(outer);

        BodyPart innerText = mock(BodyPart.class);
        when(innerText.getContentType()).thenReturn("text/html; charset=utf-8");
        when(innerText.getContent()).thenReturn("<b>Inner</b>");
        when(innerText.getDisposition()).thenReturn(null);

        BodyPart innerWeird = mock(BodyPart.class);
        when(innerWeird.getContentType()).thenReturn("application/octet-stream");
        // content is String while contentType not text, should still be included if not attachment
        when(innerWeird.getContent()).thenReturn("STRING-NON-TEXT");
        when(innerWeird.getDisposition()).thenReturn(null);

        when(inner.getCount()).thenReturn(2);
        when(inner.getBodyPart(0)).thenReturn(innerText);
        when(inner.getBodyPart(1)).thenReturn(innerWeird);

        BodyPart nestedPart = mock(BodyPart.class);
        when(nestedPart.getContentType()).thenReturn("multipart/mixed");
        when(nestedPart.getContent()).thenReturn(inner);
        when(nestedPart.getDisposition()).thenReturn(null);

        when(outer.getCount()).thenReturn(1);
        when(outer.getBodyPart(0)).thenReturn(nestedPart);

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getAttachments()).isEmpty();
        assertThat(em.getContent()).contains("Inner");
        assertThat(em.getContent()).contains("STRING-NON-TEXT");
    }


    @Test
    void fromJakartaMessage_withGmailForwardedContent_setsOriginalSender() throws Exception
    {
        // given
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(10);
        when(message.getSubject()).thenReturn("Fwd: Invoice");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("forwarder@example.com")});
        String forwardedContent = """
                See the message below.
                
                ---------- Forwarded message ---------
                From: Original Sender <ORIGINAL@supplier.com>
                Date: Mon, 1 Jan 2024 10:00:00 +0100
                Subject: Invoice
                To: forwarder@example.com
                
                Here is your invoice.
                """;
        when(message.getContent()).thenReturn(forwardedContent);

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getOriginalSender()).isEqualTo("original@supplier.com");
    }


    @Test
    void fromJakartaMessage_withOutlookForwardedContent_setsOriginalSender() throws Exception
    {
        // given
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(11);
        when(message.getSubject()).thenReturn("FW: Report");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("forwarder@example.com")});
        String forwardedContent = "See below.\r\n\r\n-----Original Message-----\r\nFrom: reporter@company.hu\r\nSent: Monday, January 1, 2024\r\nSubject: Report\r\n\r\nThe report content.";
        when(message.getContent()).thenReturn(forwardedContent);

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getOriginalSender()).isEqualTo("reporter@company.hu");
    }


    @Test
    void fromJakartaMessage_withHungarianForwardedContent_setsOriginalSender() throws Exception
    {
        // given
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(13);
        when(message.getSubject()).thenReturn("Fw: Számla");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("tovabbito@pelda.hu")});
        String forwardedContent = """
                Kérlek nézd meg az alábbi levelet.
                
                Továbbított levél kezdete:
                Feladó: Eredeti Feladó <SZAMLA@szallito.hu>
                Dátum: 2024. január 1., hétfő 10:00
                Tárgy: Számla
                Címzett: tovabbito@pelda.hu
                
                Mellékelem a számlát.
                """;
        when(message.getContent()).thenReturn(forwardedContent);

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getOriginalSender()).isEqualTo("szamla@szallito.hu");
    }


    @Test
    void fromJakartaMessage_withGermanForwardedContent_setsOriginalSender() throws Exception
    {
        // given
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(14);
        when(message.getSubject()).thenReturn("WG: Rechnung");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("weiterleitend@beispiel.de")});
        String forwardedContent = "Siehe unten.\r\n\r\n-----Ursprüngliche Nachricht-----\r\nVon: rechnung@lieferant.de\r\nGesendet: Montag, 1. Januar 2024\r\nBetreff: Rechnung\r\n\r\nIm Anhang die Rechnung.";
        when(message.getContent()).thenReturn(forwardedContent);

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getOriginalSender()).isEqualTo("rechnung@lieferant.de");
    }


    @Test
    void fromJakartaMessage_withNonForwardedContent_originalSenderIsNull() throws Exception
    {
        // given
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(12);
        when(message.getSubject()).thenReturn("Regular email");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@example.com")});
        when(message.getContent()).thenReturn("Just a regular message without any forwarding.");

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getOriginalSender()).isNull();
    }


    @Test
    void fromJakartaMessage_withGmailAutoForward_gmailDomain_resolvesSenderAndOriginalSender() throws Exception
    {
        // given – Gmail automatikus forward: Return-Path tartalmazza a +caf_= mintát @gmail.com domainnel
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(20);
        when(message.getSubject()).thenReturn("Számla");
        when(message.getSentDate()).thenReturn(new Date());
        // A From header az EREDETI feladót tartalmazza (Gmail nem írja felül)
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("automata@matusz-vad.hu")});
        when(message.getContent()).thenReturn("Számla tartalma.");
        // Return-Path: a Gmail fiók localpart+caf_=...@gmail.com formátumban
        when(message.getHeader("Return-Path")).thenReturn(new String[]{"mebilszamla+caf_=szamla=mebil.io@gmail.com"});

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getSenderEmail()).isEqualTo("mebilszamla@gmail.com");
        assertThat(em.getOriginalSender()).isEqualTo("automata@matusz-vad.hu");
    }


    @Test
    void fromJakartaMessage_withGmailAutoForward_workspaceDomain_resolvesSenderAndOriginalSender() throws Exception
    {
        // given – Google Workspace automatikus forward: Return-Path @marischka.hu domainnel (nem @gmail.com)
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(21);
        when(message.getSubject()).thenReturn("Számla");
        when(message.getSentDate()).thenReturn(new Date());
        // A From header az EREDETI feladót tartalmazza
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("automata@matusz-vad.hu")});
        when(message.getContent()).thenReturn("Számla tartalma.");
        // Return-Path: szamlazas+caf_=szamla=mebil.io@marischka.hu
        when(message.getHeader("Return-Path")).thenReturn(new String[]{"szamlazas+caf_=szamla=mebil.io@marischka.hu"});

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getSenderEmail()).isEqualTo("szamlazas@marischka.hu");
        assertThat(em.getOriginalSender()).isEqualTo("automata@matusz-vad.hu");
    }


    @Test
    void fromJakartaMessage_withGmailAutoForward_xForwardedFor_resolvesSenderAndOriginalSender() throws Exception
    {
        // given – Gmail forward ahol a Return-Path NEM tartalmaz +caf_= mintát,
        // de az X-Forwarded-For header azonosítja a továbbító Gmail/Workspace fiókot.
        // Konkrét eset: no-reply@chefmarket.hu → szamlazas@marischka.hu → szamla@mebil.io
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(22);
        when(message.getSubject()).thenReturn("Számla");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("no-reply@chefmarket.hu")});
        when(message.getContent()).thenReturn("Számla tartalma.");
        when(message.getHeader("Return-Path")).thenReturn(new String[]{"no-reply@chefmarket.hu"});
        when(message.getHeader("X-Forwarded-For")).thenReturn(new String[]{"szamlazas@marischka.hu szamla@mebil.io"});

        // when
        EmailMessage em = EmailMessage.withMetadataAndContent(message);

        // then
        assertThat(em.getSenderEmail()).isEqualTo("szamlazas@marischka.hu");
        assertThat(em.getOriginalSender()).isEqualTo("no-reply@chefmarket.hu");
    }


    @Test
    void fromJakartaMessage_whenAttachmentReadFails_wrapsIntoImapException() throws Exception
    {
        // given
        Message message = mock(Message.class);
        when(message.getMessageNumber()).thenReturn(7);
        when(message.getSubject()).thenReturn("Err");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.getFrom()).thenReturn(new Address[]{new InternetAddress("err@example.com")});

        Multipart mp = mock(Multipart.class);
        when(message.getContent()).thenReturn(mp);

        BodyPart faulty = mock(BodyPart.class);
        when(faulty.getFileName()).thenReturn("faulty.bin");
        when(faulty.getContentType()).thenReturn("application/octet-stream");
        when(faulty.getDisposition()).thenReturn(Part.ATTACHMENT);
        when(faulty.getInputStream()).thenThrow(new IOException("boom"));

        when(mp.getCount()).thenReturn(1);
        when(mp.getBodyPart(0)).thenReturn(faulty);

        // when / then
        EmailMessage emailMessage = EmailMessage.withMetadataAndContent(message);
        assertThat(emailMessage).isNotNull();
        assertThat(emailMessage.getMessageId()).isEqualTo("7");
        assertThat(emailMessage.getAttachments()).isEmpty();
    }
}
