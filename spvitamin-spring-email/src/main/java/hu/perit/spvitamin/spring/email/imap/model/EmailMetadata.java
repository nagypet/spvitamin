package hu.perit.spvitamin.spring.email.imap.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;

@Data
public class EmailMetadata
{
    private final String subject;
    private final String senderEmail;
    private final String senderName;
    private final String originalSender; // in case of a forwarded email
    private final OffsetDateTime sentDate;
    private final OffsetDateTime receivedDate;
    private final int sizeMb;
    private final String contentType;
    private final String inReplyTo;


    public boolean isValid()
    {
        return StringUtils.isNotBlank(this.senderEmail) && this.sentDate != null && this.receivedDate != null;
    }
}
