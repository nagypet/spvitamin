/*
 * Copyright 2020-2020 the original author or authors.
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

package hu.perit.spvitamin.spring.zuul;


import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.exceptionhandler.RestExceptionResponse;
import hu.perit.spvitamin.spring.json.JSonSerializer;
import lombok.extern.log4j.Log4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.*;

/**
 * #know-how:custom-zuul-error-filter
 *
 * @author Peter Nagy
 */

@Component
@Log4j
public class CustomZuulErrorFilter extends ZuulFilter {

    private static final String SEND_ERROR_FILTER_RAN = "sendErrorFilter.ran";

    @Override
    public String filterType() {
        return ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return SEND_ERROR_FILTER_ORDER - 1; // Needs to run before SendErrorFilter which has filterOrder == 0
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        Throwable ex = ctx.getThrowable();
        return ex instanceof ZuulException && !ctx.getBoolean(SEND_ERROR_FILTER_RAN, false);
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            ZuulException ex = (ZuulException) ctx.getThrowable();

            // log this as error
            log.error(StackTracer.toString(ex));

            String requestUri = ctx.containsKey(REQUEST_URI_KEY) ? ctx.get(REQUEST_URI_KEY).toString() : "/";
            RestExceptionResponse exceptionResponse = new RestExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, requestUri);

            // Populate context with new response values
            ctx.setResponseStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            this.writeResponseBody(ctx.getResponse(), exceptionResponse);

            ctx.set(SEND_ERROR_FILTER_RAN, true);
        }
        catch (Exception ex) {
            log.error(StackTracer.toString(ex));
            ReflectionUtils.rethrowRuntimeException(ex);
        }
        return null;
    }


    private void writeResponseBody(HttpServletResponse response, Object body) throws IOException {
        response.setContentType("application/json");
        try (PrintWriter writer = response.getWriter()) {
            writer.println(new JSonSerializer().toJson(body));
        }
    }
}