package hu.perit.spvitamin.spring.logging;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Data
public class LogEvent
{
    private LocalDateTime eventTime;
    private boolean directionInput;
    private String clientIpAddr;
    private String traceId;
    private String user;
    private String hostName;
    private String subsystemName;
    private int eventId;
    private String eventText;
    private String parameters;

    @Override
    public String toString()
    {
        return String.format("%s | %s | %s | user: %s | host: %s | system: %s | eventId: %d | event: %s | %s ",
                this.directionInput ? ">>>" : "<<<",
                this.clientIpAddr,
                StringUtils.defaultIfBlank(this.traceId, "null"),
                StringUtils.defaultIfBlank(this.user, "null"),
                StringUtils.defaultString(this.hostName, "null"),
                StringUtils.defaultIfBlank(this.subsystemName, "null"),
                this.eventId,
                StringUtils.defaultIfBlank(this.eventText, "null"),
                StringUtils.defaultIfBlank(this.parameters, "null"));
    }
}
