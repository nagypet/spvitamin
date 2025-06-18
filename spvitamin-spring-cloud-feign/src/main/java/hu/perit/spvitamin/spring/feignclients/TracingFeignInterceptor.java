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

import feign.RequestInterceptor;
import feign.RequestTemplate;
import hu.perit.spvitamin.spring.config.SpringContext;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

public class TracingFeignInterceptor implements RequestInterceptor
{
    @Override
    public void apply(RequestTemplate requestTemplate)
    {
        Tracer tracer = SpringContext.getBean(Tracer.class);
        String traceContextIds = getTraceContextIds(tracer);
        if (traceContextIds != null)
        {
            requestTemplate.header("traceparent", getTraceParentHeader(traceContextIds));
        }
    }

    // https://www.w3.org/TR/trace-context/#header-name
    private static String getTraceParentHeader(String traceId)
    {
        return String.format("00-%s-01", traceId);
    }

    private static String getTraceContextIds(Tracer tracer)
    {
        if (tracer == null)
        {
            return null;
        }

        final Span span = tracer.currentSpan();
        return span != null
                ? span.context().traceId() + "-" + span.context().spanId()
                : null;
    }

}
