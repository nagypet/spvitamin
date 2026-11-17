package hu.perit.spvitamin.spring.email.imap.model;

import java.util.List;

public record EmailDownloadResult(
        List<EmailMessage> eligible,
        List<EmailMessage> unprocessable,
        int remainingCount
)
{
}
