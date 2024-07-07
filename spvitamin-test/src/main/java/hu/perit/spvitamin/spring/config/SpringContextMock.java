package hu.perit.spvitamin.spring.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpringContextMock
{
    public static Builder builder()
    {
        return new Builder();
    }

    private static void init(ApplicationContext applicationContext, Map<Class<?>, Object> beanMap)
    {
        when(applicationContext.getBean((Class<?>) any(Class.class))).thenAnswer(invocation -> {
            Class<?> requestedBean = invocation.getArgument(0);
            if (beanMap.containsKey(requestedBean))
            {
                return beanMap.get(requestedBean);
            }
            else
            {
                throw new RuntimeException("Type is not mocked: " + requestedBean.getSimpleName());
            }
        });

        ReflectionTestUtils.setField(SpringContext.class, "context", applicationContext);
    }

    public static class Builder
    {
        private final Map<Class<?>, Object> beanMap = new HashMap<>();

        public Builder append(Class<?> clazz, Object bean)
        {
            beanMap.put(clazz, bean);
            return this;
        }

        public void build(ApplicationContext applicationContext)
        {
            init(applicationContext, beanMap);
        }
    }
}
