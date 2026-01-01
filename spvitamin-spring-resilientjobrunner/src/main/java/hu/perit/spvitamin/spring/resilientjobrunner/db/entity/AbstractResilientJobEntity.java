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

package hu.perit.spvitamin.spring.resilientjobrunner.db.entity;

import hu.perit.spvitamin.core.exception.ServerException;
import hu.perit.spvitamin.json.JSonSerializer;
import hu.perit.spvitamin.json.SpvitaminObjectMapper;
import hu.perit.spvitamin.spring.data.converter.OffsetDateTimeToUTCConverter;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatus;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
public class AbstractResilientJobEntity
{
    public static final String COL_ID = "id";
    public static final String COL_CREATION_TIMESTAMP = "creation_timestamp";
    public static final String COL_STATUS = "status";
    public static final String COL_PROCESSOR = "processor";
    public static final String COL_PARAMETER_VERSION = "parameter_version";
    public static final String COL_PARAMETERS = "parameters";
    public static final String COL_RETRY_COUNT = "retry_count";
    public static final String COL_PROCESSING_STARTED_TIMESTAMP = "processing_started_timestamp";
    public static final String COL_EROR_TEXT = "error_text";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_generator")
    @NotNull
    @Column(name = COL_ID, nullable = false)
    private Long id;

    @Column(name = COL_CREATION_TIMESTAMP)
    @Convert(converter = OffsetDateTimeToUTCConverter.class)
    private OffsetDateTime creationTimestamp;

    @NotNull
    @Column(name = COL_STATUS, nullable = false)
    @Convert(converter = ResilientJobStatusConverter.class)
    private ResilientJobStatus status;

    @NotNull
    @Column(name = COL_PROCESSOR, nullable = false)
    private Long processorType;

    @NotNull
    @Column(name = COL_PARAMETER_VERSION, nullable = false)
    private Integer parameterVersion;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @NotNull
    @Column(name = COL_PARAMETERS, nullable = false, columnDefinition = "TEXT")
    private String parameters;

    @Column(name = COL_PROCESSING_STARTED_TIMESTAMP)
    @Convert(converter = OffsetDateTimeToUTCConverter.class)
    private OffsetDateTime processingStartedTimestamp;

    @Column(name = COL_EROR_TEXT, columnDefinition = "TEXT")
    private String errorText;

    @Column(name = COL_RETRY_COUNT)
    private Long retryCount;


    public void setParameters(Object data)
    {
        try
        {
            JsonMapper mapper = SpvitaminObjectMapper.getJsonMapper();
            // TODO
            //mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            this.parameters = mapper.writeValueAsString(data);
        }
        catch (JacksonException e)
        {
            ServerException.throwFrom(e);
        }
    }


    public <T> T getParameters(Class<T> clazz)
    {
        try
        {
            return JSonSerializer.fromJson(this.parameters, clazz);
        }
        catch (JacksonException e)
        {
            return ServerException.throwFrom(e);
        }
    }
}
