/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.config;

import jakarta.validation.*;

import java.util.Set;

/**
 * @author Peter Nagy
 */


public class SysConfig
{

    public static JwtProperties getJwtProperties()
    {
        return validate(SpringContext.getBean(JwtProperties.class));
    }

    public static JwtPropertiesPublic getJwtPropertiesPublic()
    {
        return validate(SpringContext.getBean(JwtPropertiesPublic.class));
    }

    public static SecurityProperties getSecurityProperties()
    {
        return SpringContext.getBean(SecurityProperties.class);
    }

    public static ServerProperties getServerProperties()
    {
        return SpringContext.getBean(ServerProperties.class);
    }

    public static MetricsProperties getMetricsProperties()
    {
        return SpringContext.getBean(MetricsProperties.class);
    }

    public static CryptoProperties getCryptoProperties()
    {
        return SpringContext.getBean(CryptoProperties.class);
    }

    public static AdminProperties getAdminProperties()
    {
        return SpringContext.getBean(AdminProperties.class);
    }

    public static MicroserviceCollectionProperties getSysMicroservices()
    {
        return SpringContext.getBean(MicroserviceCollectionProperties.class);
    }

    public static FeignProperties getFeignProperties()
    {
        return SpringContext.getBean(FeignProperties.class);
    }


    private static <T> T validate(T bean)
    {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory())
        {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<T>> violations = validator.validate(bean);
            if (!violations.isEmpty())
            {
                throw new ConstraintViolationException(violations);
            }
        }
        return bean;
    }
}
