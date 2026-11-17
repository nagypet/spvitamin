package hu.perit.spvitamin.spring.email.imap.model;

import lombok.Data;
import lombok.Generated;

/**
 * Represents an email attachment.
 */
@Data
@Generated // To disable counting in unit test coverage
public final class EmailAttachment
{
    private final String filename;
    private final String contentType;
    private final byte[] data;
}
