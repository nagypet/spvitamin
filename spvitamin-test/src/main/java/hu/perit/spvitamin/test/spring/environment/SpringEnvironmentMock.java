package hu.perit.spvitamin.test.spring.environment;

import hu.perit.spvitamin.spring.environment.SpringEnvironment;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpringEnvironmentMock
{
    public static Builder builder()
    {
        return new Builder();
    }

    private static void init(Environment environment, Map<String, String> propertyMap)
    {
        lenient().when(environment.getProperty(anyString())).thenAnswer(invocation -> {
            String requestedKey = invocation.getArgument(0);
            if (propertyMap.containsKey(requestedKey))
            {
                return propertyMap.get(requestedKey);
            }
            else
            {
                throw new RuntimeException("Key not found in mocked environment: " + requestedKey);
            }
        });

        SpringEnvironment.setEnvironment(environment);
    }

    public static class Builder
    {
        private final Map<String, String> propertyMap = new HashMap<>();

        public Builder append(String key, String value)
        {
            propertyMap.put(key, value);
            return this;
        }

        public void build(Environment environment)
        {
            init(environment, propertyMap);
        }
    }
}
