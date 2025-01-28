
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

package hu.perit.spvitamin.spring.exceptionhandler;

import hu.perit.spvitamin.spring.config.ServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Peter Nagy
 */


@Component
@Slf4j
public class CustomErrorAttributes extends DefaultErrorAttributes
{

    private final ServerProperties serverProperties;


    public CustomErrorAttributes(ServerProperties serverProperties)
    {
        this.serverProperties = serverProperties;
    }


    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options)
    {
        Map<String, Object> attributes = super.getErrorAttributes(webRequest, options);

        logAttributes(attributes);

        Object status = webRequest.getAttribute("javax.servlet.error.status_code", RequestAttributes.SCOPE_REQUEST);

        if ((status instanceof Integer statusValue) && (statusValue == 404))
        {
            Object message = webRequest.getAttribute("javax.servlet.error.message", RequestAttributes.SCOPE_REQUEST);
            if (message == null || message.toString().equals(""))
            {
                Object path = webRequest.getAttribute("javax.servlet.error.request_uri", RequestAttributes.SCOPE_REQUEST);
                String link = String.format("%s://%s:%s", "http(s)", this.serverProperties.getFqdn(), this.serverProperties.getPort());
                message = String.format("There is no handler for '%s%s'! For available services please see: '%s'", link, path, link);
                attributes.put("message", message);
            }
        }

        return attributes;
    }


    private void logAttributes(Map<String, Object> attributes)
    {
        String errorText = attributes.entrySet().stream()
            .filter(i -> !"trace".equalsIgnoreCase(i.getKey()))
            .map(e -> String.format("%s: %s", e.getKey(), e.getValue()))
            .collect(Collectors.joining("; "));
        log.warn(errorText);
    }
}
