package hu.perit.spvitamin.spring.tokencache;

import hu.perit.spvitamin.core.cache.CacheableEntity;
import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
@RequiredArgsConstructor
@ToString
public class ArchiveSystemToken implements CacheableEntity
{
    private final AuthorizationToken token;
    private final Instant receivedAt = Instant.now();


    @Override
    public boolean isValid()
    {
        if (token == null || token.getJwt() == null)
        {
            return false;
        }
        // Considering clock skew
        Duration validity = Duration.between(token.getIat(), token.getExp()).truncatedTo(ChronoUnit.SECONDS);
        Instant exp = this.receivedAt.plus(validity).minusSeconds(60);
        return Instant.now().isBefore(exp);
    }
}
