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
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

// @formatter:off
/**
 * @Getter
 * @Setter
 * @Entity
 * @Table(name = ResilientJobEntity.TABLE_NAME, schema = Constants.SCHEMA, indexes = {
 *         @Index(name = ResilientJobEntity.IX_CREATION_TIMESTAMP, columnList = AbstractResilientJobEntity.COL_CREATION_TIMESTAMP),
 *         @Index(name = ResilientJobEntity.IX_STATUS, columnList = AbstractResilientJobEntity.COL_STATUS),
 *         @Index(name = ResilientJobEntity.IX_PROCESSING_FIRST_STARTED_TIMESTAMP, columnList = AbstractResilientJobEntity.COL_PROCESSING_FIRST_STARTED_TIMESTAMP),
 *         @Index(name = ResilientJobEntity.IX_PROCESSING_LAST_STARTED_TIMESTAMP, columnList = AbstractResilientJobEntity.COL_PROCESSING_LAST_STARTED_TIMESTAMP),
 *         @Index(name = ResilientJobEntity.IX_NEXT_RETRY_TIMESTAMP, columnList = AbstractResilientJobEntity.COL_NEXT_RETRY_TIMESTAMP),
 *         @Index(name = ResilientJobEntity.IX_PARAMETER_HASH, columnList = AbstractResilientJobEntity.COL_PARAMETER_HASH)
 * })
 * @SequenceGenerator(name = "job_generator", sequenceName = "job_seq", schema = Constants.SCHEMA, allocationSize = 1)
 * @ToString(callSuper = true)
 * @Generated // To disable counting in unit test coverage
 * public class ResilientJobEntity extends AbstractResilientJobEntity
 * {
 *     public static final String TABLE_NAME = "job";
 *
 *     public static final String IX_PREFIX = "ix_email_log_";
 *     public static final String IX_CREATION_TIMESTAMP = IX_PREFIX + "01";
 *     public static final String IX_STATUS = IX_PREFIX + "02";
 *     public static final String IX_PROCESSING_FIRST_STARTED_TIMESTAMP = IX_PREFIX + "03";
 *     public static final String IX_PROCESSING_LAST_STARTED_TIMESTAMP = IX_PREFIX + "04";
 *     public static final String IX_NEXT_RETRY_TIMESTAMP = IX_PREFIX + "05";
 *     public static final String IX_PARAMETER_HASH = IX_PREFIX + "06";
 * }
 */
// @formatter:on

@Getter
@Setter
@MappedSuperclass
@ToString
public class AbstractResilientJobEntity
{
    public static final String COL_ID = "id";
    public static final String COL_CREATION_TIMESTAMP = "creation_timestamp";
    public static final String COL_STATUS = "status";
    public static final String COL_PROCESSOR = "processor";
    public static final String COL_PARAMETER_VERSION = "parameter_version";
    public static final String COL_PARAMETERS = "parameters";
    public static final String COL_RETRY_COUNT = "retry_count";
    public static final String COL_PROCESSING_FIRST_STARTED_TIMESTAMP = "processing_first_started_timestamp";
    public static final String COL_PROCESSING_LAST_STARTED_TIMESTAMP = "processing_last_started_timestamp";
    public static final String COL_NEXT_RETRY_TIMESTAMP = "next_retry_timestamp";
    public static final String COL_ERROR_TEXT = "error_text";
    public static final String COL_PARAMETER_HASH = "parameter_hash";
    public static final String COL_SAGA_CONTEXT = "saga_context";
    public static final String COL_SAGA_CONTEXT_VERSION = "saga_context_version";
    public static final String COL_LAST_STEP  = "last_step";
    public static final String COL_OPERATION_ID  = "operation_id";

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

    @NotNull
    @Column(name = COL_PARAMETERS, nullable = false, columnDefinition = "TEXT")
    private String parameters;

    @Column(name = COL_PROCESSING_FIRST_STARTED_TIMESTAMP)
    @Convert(converter = OffsetDateTimeToUTCConverter.class)
    private OffsetDateTime processingFirstStartedTimestamp;

    @Column(name = COL_PROCESSING_LAST_STARTED_TIMESTAMP)
    @Convert(converter = OffsetDateTimeToUTCConverter.class)
    private OffsetDateTime processingLastStartedTimestamp;

    @Column(name = COL_NEXT_RETRY_TIMESTAMP)
    @Convert(converter = OffsetDateTimeToUTCConverter.class)
    private OffsetDateTime nextRetryTimestamp;

    @Column(name = COL_ERROR_TEXT, columnDefinition = "TEXT")
    private String errorText;

    @Column(name = COL_RETRY_COUNT)
    private Long retryCount;

    // This can be used as an idempotent key
    @NotNull
    @Size(max = 50)
    @Column(name = COL_PARAMETER_HASH, nullable = false)
    private String parameterHash;

    // A lépésenként felhalmozódó, MUTÁBILIS állapot (JSON). A parameters immutábilis marad.
    @Column(name = COL_SAGA_CONTEXT, columnDefinition = "TEXT")
    private String sagaContext;

    @Column(name = COL_SAGA_CONTEXT_VERSION)
    private Integer sagaContextVersion;

    // Az utoljára sikeresen befejezett lépés neve (diagnosztika / resume).
    @Column(name = COL_LAST_STEP)
    private String sagaLastStep;

    // This will be generated at creation and can be used as an idempotency key.
    @Column(name = COL_OPERATION_ID)
    private UUID operationId;
}
