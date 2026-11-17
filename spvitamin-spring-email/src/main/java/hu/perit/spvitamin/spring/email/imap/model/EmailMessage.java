package hu.perit.spvitamin.spring.email.imap.model;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.crypto.HashUtils;
import hu.perit.spvitamin.json.typehelpers.OffsetDateTimeUtils;
import hu.perit.spvitamin.spring.email.imap.util.SenderResolver;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
public class EmailMessage
{
    private final String messageId;
    @Delegate
    private final EmailMetadata metadata;
    private final String content;
    private final List<EmailAttachment> attachments;
    /**
     * The {@code Message-ID} header extracted from the embedded {@code message/rfc822} part
     * of a DSN bounce email (RFC 3464, Part 3). This is the most reliable source for bounce
     * correlation: it is the original email's {@code Message-ID} as attached by the receiving MTA.
     * Null for non-DSN messages and for DSN messages that do not contain a {@code message/rfc822} part.
     */
    private final String rfc822MessageId;


    /**
     * Full constructor. Use when parsing DSN bounce messages where the rfc822 part is available.
     */
    public EmailMessage(String messageId, EmailMetadata metadata, String content, List<EmailAttachment> attachments, String rfc822MessageId)
    {
        Objects.requireNonNull(messageId, "messageId");
        this.messageId = messageId;
        this.metadata = metadata != null
                ? metadata
                : new EmailMetadata(
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                null
        );
        this.content = content;
        this.attachments = attachments != null ? attachments : List.of();
        this.rfc822MessageId = rfc822MessageId;
    }


    /**
     * Backward-compatible constructor (rfc822MessageId = null).
     */
    public EmailMessage(String messageId, EmailMetadata metadata, String content, List<EmailAttachment> attachments)
    {
        this(messageId, metadata, content, attachments, null);
    }


    public static EmailMessage withMetadata(Message message)
    {
        // Use message number as a unique identifier
        String messageId = String.valueOf(message.getMessageNumber());

        try
        {
            log.info("Loading metadata from message {}. Subject: {} from: {}, size: {}", messageId, message.getSubject(), message.getFrom(), message.getSize());

            SenderInfo senderInfo = SenderResolver.resolveSenderInfo(message, null);

            EmailMetadata metadata = new EmailMetadata(
                    message.getSubject(),
                    senderInfo.senderEmail(),
                    senderInfo.senderName(),
                    senderInfo.originalSender(),
                    OffsetDateTimeUtils.fromDate(message.getSentDate()),
                    OffsetDateTimeUtils.fromDate(message.getReceivedDate()),
                    toMB(message.getSize()),
                    null,
                    null
            );

            return new EmailMessage(
                    messageId,
                    metadata,
                    null,
                    null
            );
        }
        catch (MessagingException | RuntimeException e)
        {
            log.error("Failed to process email message {}: {}", messageId, StackTracer.toString(e));
            return new EmailMessage(
                    messageId,
                    null,
                    null,
                    null
            );
        }
    }


    static int toMB(int size)
    {
        return size / 1024 / 1024;
    }


    public static EmailMessage withMetadataAndContent(Message message)
    {
        // Use message number as a unique identifier
        String messageId = String.valueOf(message.getMessageNumber());

        try
        {
            log.info("Loading content from message {}. Subject: {} from: {}, size: {}", messageId, message.getSubject(), message.getFrom(), message.getSize());

            Object rawContent = message.getContent();
            String content = extractContent(rawContent);
            List<EmailAttachment> attachments = extractAttachments(rawContent);
            String rfc822MessageId = extractRfc822MessageId(rawContent);

            SenderInfo senderInfo = SenderResolver.resolveSenderInfo(message, content);

            String contentType = message.getContentType();
            String[] inReplyToHeaders = message.getHeader("In-Reply-To");
            String inReplyTo = (inReplyToHeaders != null && inReplyToHeaders.length > 0) ? inReplyToHeaders[0] : null;

            EmailMetadata metadata = new EmailMetadata(
                    message.getSubject(),
                    senderInfo.senderEmail(),
                    senderInfo.senderName(),
                    senderInfo.originalSender(),
                    OffsetDateTimeUtils.fromDate(message.getSentDate()),
                    OffsetDateTimeUtils.fromDate(message.getReceivedDate()),
                    toMB(message.getSize()),
                    contentType,
                    inReplyTo
            );

            return new EmailMessage(
                    messageId,
                    metadata,
                    content,
                    attachments,
                    rfc822MessageId
            );
        }
        catch (MessagingException | IOException | RuntimeException e)
        {
            log.error("Failed to process email message {}: {}", messageId, StackTracer.toString(e));
            return withMetadata(message);
        }
    }


    public boolean isValid()
    {
        return StringUtils.isNotBlank(this.messageId) && this.metadata != null && this.metadata.isValid() && this.content != null;
    }


    /**
     * Extracts the {@code Message-ID} header from the embedded {@code message/rfc822} part of a
     * DSN multipart/report message (RFC 3464, Part 3 – the original failed message).
     * <p>
     * This is the most reliable source for bounce correlation: the original email is literally
     * attached to the bounce, and its {@code Message-ID} header uniquely identifies it.
     *
     * @return the raw {@code Message-ID} value (including surrounding angle brackets if present),
     *         or {@code null} if no {@code message/rfc822} part is found
     */
    private static String extractRfc822MessageId(Object messageContent)
    {
        try
        {
            if (!(messageContent instanceof Multipart multipart))
            {
                return null;
            }
            for (int i = 0; i < multipart.getCount(); i++)
            {
                BodyPart part = multipart.getBodyPart(i);
                if (part.getContentType().toLowerCase().startsWith("message/rfc822"))
                {
                    Object partContent = part.getContent();
                    if (partContent instanceof Message embeddedMessage)
                    {
                        String[] headers = embeddedMessage.getHeader("Message-ID");
                        if (headers != null && headers.length > 0 && StringUtils.isNotBlank(headers[0]))
                        {
                            return headers[0].trim();
                        }
                    }
                }
            }
        }
        catch (MessagingException | IOException | RuntimeException e)
        {
            log.warn("Could not extract rfc822 Message-ID from bounce: {}", e.getMessage());
        }
        return null;
    }


    private static String extractContent(Object messageContent)
    {
        try
        {
            if (messageContent instanceof String stringContent)
            {
                return stringContent;
            }
            else if (messageContent instanceof Multipart multipartContent)
            {
                return processMultipartContent(multipartContent);
            }
            else
            {
                return messageContent.toString();
            }
        }
        catch (Exception e)
        {
            return "Failed to extract content: " + e.getMessage();
        }
    }


    private static String processMultipartContent(Multipart multipartContent) throws MessagingException, IOException
    {
        StringBuilder contentBuilder = new StringBuilder();

        for (int i = 0; i < multipartContent.getCount(); i++)
        {
            BodyPart bodyPart = multipartContent.getBodyPart(i);
            String contentType = bodyPart.getContentType().toLowerCase();
            Object content = bodyPart.getContent();

            // Handle nested multipart content recursively
            if (content instanceof Multipart nestedMultipart)
            {
                contentBuilder.append(processMultipartContent(nestedMultipart));
            }
            // Handle text content
            else if (contentType.contains("text/plain") || contentType.contains("text/html"))
            {
                contentBuilder.append(content.toString());
            }
            // Include delivery-status parts for DSN/bounce parsing
            else if (contentType.contains("message/delivery-status"))
            {
                contentBuilder.append(content.toString());
            }
            // Try to get content even if content type is not recognized as text
            else if (!isAttachment(bodyPart.getDisposition()) && content instanceof String)
            {
                contentBuilder.append(content);
            }
        }

        return contentBuilder.toString();
    }


    private static List<EmailAttachment> extractAttachments(Object messageContent) throws MessagingException, IOException
    {
        List<EmailAttachment> attachments = new ArrayList<>();

        if (messageContent instanceof Multipart multipartContent)
        {
            for (int i = 0; i < multipartContent.getCount(); i++)
            {
                BodyPart bodyPart = multipartContent.getBodyPart(i);
                String disposition = bodyPart.getDisposition();

                if (isAttachment(disposition))
                {
                    EmailAttachment attachment = processAttachment(bodyPart);
                    if (attachment != null)
                    {
                        attachments.add(attachment);
                    }
                }
            }
        }

        return attachments;
    }


    private static boolean isAttachment(String disposition)
    {
        return disposition != null &&
                (disposition.equalsIgnoreCase(Part.ATTACHMENT) ||
                        disposition.equalsIgnoreCase(Part.INLINE));
    }


    private static EmailAttachment processAttachment(BodyPart bodyPart) throws MessagingException, IOException
    {
        String fileName = bodyPart.getFileName();
        if (fileName == null)
        {
            return null;
        }

        String contentType = bodyPart.getContentType().toLowerCase();
        byte[] attachmentData = readAttachmentData(bodyPart);

        return new EmailAttachment(fileName, contentType, attachmentData);
    }


    private static byte[] readAttachmentData(BodyPart bodyPart) throws MessagingException, IOException
    {
        try (InputStream is = bodyPart.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1)
            {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }


    public String getSignature()
    {
        String normalizedSubject = normalizeForSignature(getSubject());
        String normalizedSender = normalizeForSignature(getSenderEmail()).toLowerCase();

        // Pick a stable sentDate representation:
        String normalizedSentDate = normalizeSentDateForSignature(getSentDate());

        String canonical = normalizedSentDate + "|" + normalizedSender + "|" + normalizedSubject;
        return HashUtils.get32BytesSha256Hash(canonical);
    }


    private static String normalizeForSignature(String value)
    {
        if (value == null)
        {
            return "";
        }
        String s = StringUtils.trim(value);
        // collapse whitespace
        return s.replaceAll("\\s+", " ");
    }


    private static String normalizeSentDateForSignature(OffsetDateTime sentDate)
    {
        if (sentDate == null)
        {
            return "";
        }
        return String.valueOf(sentDate.toInstant().toEpochMilli());
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("metadata", metadata)
                .append("content", StringUtils.truncate(content, 200))
                .append("attachments", attachments.size())
                .toString();
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        EmailMessage that = (EmailMessage) o;

        return new EqualsBuilder()
                .append(getSignature(), that.getSignature())
                .isEquals();
    }


    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(getSignature()).toHashCode();
    }
}
