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

package hu.perit.spvitamin.spring.rest;

import hu.perit.spvitamin.spring.exceptionhandler.RestExceptionResponse;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

/**
 * Springdoc {@link OperationCustomizer} that processes {@link StandardApiResponses} annotations
 * and adds the corresponding OpenAPI response descriptions to the generated documentation.
 *
 * @see StandardApiResponsesConfiguration
 */
public class StandardApiResponsesCustomizer implements OperationCustomizer
{
    private static final String ERROR_SCHEMA_REF = "#/components/schemas/" + RestExceptionResponse.class.getSimpleName();


    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod)
    {
        StandardApiResponses annotation = handlerMethod.getMethodAnnotation(StandardApiResponses.class);
        if (annotation == null)
        {
            return operation;
        }

        if (operation.getResponses() == null)
        {
            operation.setResponses(new ApiResponses());
        }

        for (int code : annotation.codes())
        {
            String codeStr = String.valueOf(code);
            if (isSuccessCode(code))
            {
                // Springdoc already generated the success response with the method's return type schema.
                // Only set the description so we don't lose the schema.
                ApiResponse existing = operation.getResponses().get(codeStr);
                if (existing != null)
                {
                    existing.setDescription(HttpStatus.valueOf(code).getReasonPhrase());
                }
                else
                {
                    operation.getResponses().addApiResponse(codeStr, new ApiResponse().description(HttpStatus.valueOf(code).getReasonPhrase()));
                }
            }
            else
            {
                operation.getResponses().addApiResponse(codeStr, buildErrorResponse(code));
            }
        }

        return operation;
    }


    private boolean isSuccessCode(int code)
    {
        return code >= 200 && code < 300;
    }


    private ApiResponse buildErrorResponse(int code)
    {
        return new ApiResponse()
                .description(HttpStatus.valueOf(code).getReasonPhrase())
                .content(errorContent());
    }


    private Content errorContent()
    {
        Schema<RestExceptionResponse> schema = new Schema<RestExceptionResponse>().$ref(ERROR_SCHEMA_REF);
        return new Content().addMediaType("application/json", new MediaType().schema(schema));
    }
}
