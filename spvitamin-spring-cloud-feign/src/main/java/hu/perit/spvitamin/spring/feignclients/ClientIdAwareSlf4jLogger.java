package hu.perit.spvitamin.spring.feignclients;

import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class ClientIdAwareSlf4jLogger extends Slf4jLogger
{
    private final String clientId;


    public ClientIdAwareSlf4jLogger(Class<?> type, String clientId)
    {
        super(type);
        this.clientId = clientId;
    }


    @Override
    protected void log(String configKey, String format, Object... args)
    {
        String tag = prefix(configKey);
        log.info(String.format(tag + format, args));
    }


    private String prefix(String configKey)
    {
        String methodKey = configKey.substring(0, configKey.indexOf('('));
        if (StringUtils.isBlank(this.clientId))
        {
            return '[' + methodKey + "] ";
        }
        return '[' + this.clientId + "#" + methodKey + "] ";
    }
}
