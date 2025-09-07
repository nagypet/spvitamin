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

package hu.perit.spvitamin.spring.feignclients;

import feign.Client;
import feign.Request;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public final class HeaderFilterFeignClient implements Client
{
    private final Client delegate;


    @Override
    public Response execute(Request request, Request.Options options) throws IOException
    {
        Response rawResponse = delegate.execute(request, options);
        Map<String, Collection<String>> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, Collection<String>> entry : rawResponse.headers().entrySet())
        {
            String name = entry.getKey();
            if (StringUtils.equalsAnyIgnoreCase(name, "set-cookie", "set-cookie2"))
            {
                log.info("Header removed {}, {}", name, entry.getValue());
            }
            else
            {
                filtered.put(name, entry.getValue());
            }
        }

        return Response.builder()
                .status(rawResponse.status())
                .reason(rawResponse.reason())
                .request(rawResponse.request())
                .headers(filtered)
                .body(rawResponse.body())
                .build();
    }
}
