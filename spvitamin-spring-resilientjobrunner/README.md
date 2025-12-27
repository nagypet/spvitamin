# spvitamin-spring-resilientjobrunner

<!-- TOC -->
* [spvitamin-spring-resilientjobrunner](#spvitamin-spring-resilientjobrunner)
  * [What is this?](#what-is-this)
  * [When to use](#when-to-use)
  * [High-level architecture](#high-level-architecture)
  * [Core APIs (from this module)](#core-apis-from-this-module)
  * [Installation](#installation)
  * [Configuration](#configuration)
  * [Implementing the persistence side](#implementing-the-persistence-side)
  * [Implementing a processor](#implementing-a-processor)
  * [Enqueueing jobs](#enqueueing-jobs)
  * [How processing runs](#how-processing-runs)
  * [Exception classification cheat sheet](#exception-classification-cheat-sheet)
  * [Observability](#observability)
  * [Tips and best practices](#tips-and-best-practices)
  * [Troubleshooting](#troubleshooting)
  * [Minimal checklist to integrate](#minimal-checklist-to-integrate)
  * [License](#license)
<!-- TOC -->

## What is this?

`spvitamin-spring-resilientjobrunner` is a small Spring component for reliably processing background jobs stored in a
persistent store (typically a database).

It repeatedly polls for new jobs and processes them in parallel worker threads with:

- retry and back-off semantics driven by job age and exception classification,
- automatic recovery of jobs stuck in `IN_PROGRESS`,
- permanent termination of jobs that exceeded the configured retry window,
- plug-in processors you implement per job type,
- minimal required integration (an entity implementing `ResilientJobData` and a data service implementing
  `ResilientJobDataService`).

---

## When to use

Use this component if you need to:

- enqueue tasks into a DB table and process them asynchronously,
- ensure tasks survive restarts and temporary outages,
- classify exceptions into retryable/non-retryable and item-related/non-item-related,
- run processors in parallel with safe re-tries and idempotent behavior.

If you already have a full-fledged queue/broker (Kafka, RabbitMQ, etc.), you might not need this. If your system is
DB-centric and you want a light, Spring-native job runner, this fits well.

---

## High-level architecture

- You model a job row using an entity that implements `ResilientJobData` (e.g., `ResilientJobEntity`).
- You implement `ResilientJobDataService` to provide the minimal data operations (batch fetch + state changes).
- You implement one or more `AbstractProcessor` subclasses — one per job type — containing your domain logic and error
  handling hooks.
- You configure job types in `application.yml` under `resilient-jobs.*` (ID, processor class, thread pool size,
  exception classification, retry timeout, etc.).
- The module’s internal scheduler (`BJobProcessor`) periodically:
    - terminates very old failing jobs (past `retry-timeout`),
    - resets jobs stuck in `IN_PROGRESS` back to `CREATED`,
    - pulls a batch of `CREATED` jobs for each configured processor and executes them in parallel via a `BatchExecutor`.

Status lifecycle:

- `CREATED` → picked for execution → set to `IN_PROGRESS`
- If processing succeeds: the job row is deleted (`deleteById`).
- If exception occurs:
    - If exception is retryable OR not item-related → save error details and set back to `CREATED` (to be retried), and
      if it is retryable AND not item-related the batch is interrupted (infrastructure issue) to avoid burning through
      more items.
    - If exception is item-related and not retryable → set status to `ERROR` and invoke `onError` hook; the batch
      continues.
- Jobs older than `retry-timeout` are permanently set to `ERROR`.

---

## Core APIs (from this module)

```java
public interface ResilientJobData
{
    Long getId();
    OffsetDateTime getCreationTimestamp();
    ResilientJobStatus getStatus();
    Long getProcessorType();
    Integer getParameterVersion();
    OffsetDateTime getProcessingStartedTimestamp();
    String getErrorText();
    Long getRetryCount();
    <T> T getParameters(Class<T> clazz);
}

```

```java
public interface ResilientJobDataService
{
    int terminatePermanentlyFailingEntities(ProcessorType processorType, Duration timeout);
    int resetStuckInProgressEntities(ProcessorType processorType, Duration timeout);
    List<? extends ResilientJobData> getNextBatchAndSetInProgressState(ProcessorType processorType);
    int resetInProgressEntitiesById(List<Long> ids);
    void deleteById(Long id);
    void saveError(Long id, ResilientJobStatus resilientJobStatus, Exception e);
}
```

```java

@Getter
@RequiredArgsConstructor
public abstract class AbstractProcessor
{
    private final ProcessorType processorType;
    private final ResilientJobProperties properties;


    public abstract void processJob(ResilientJobData entity) throws Exception;

    public abstract void onError(ResilientJobData entity, Exception e);


    /**
     * If the exception is retryable, we will retry the job once again. For instance, FeignException.ServiceUnavailable.
     */
    public boolean isRetryableException(Throwable e)
    {
        return ExceptionHelper.isRetryableException(e, this.properties.getRetryableExceptions());
    }


    public boolean isItemRelatedException(Throwable e)
    {
        return ExceptionHelper.isItemRelatedException(e, this.properties.getItemRelatedExceptions());
    }
}

```

---

## Installation

1) Add module dependency to your service (Gradle example):

```
implementation project(":spvitamin:spvitamin-spring-resilientjobrunner")
```

2) Ensure scheduling is enabled in your Spring Boot app (the module uses `@Scheduled`):

```
@SpringBootApplication
@EnableScheduling
public class YourApplication { }
```

3) Optionally set the scheduler pool size (in your `application.yml`):

```
spring:
  task:
    scheduling:
      pool.size: 10
```

---

## Configuration

Define your job types under the root `resilient-jobs` property. Each entry has a unique name and maps to
`ResilientJobProperties`:

Example:

```
resilient-jobs:
  document-remover:
    id: 10
    processor-class: com.example.DocumentRemover
    thread-pool-size: 10
    retry-timeout: 1h
    retryable-exceptions:
      - "feign.RetryableException"
      - "feign.FeignException$GatewayTimeout"
      - "feign.FeignException$ServiceUnavailable"
      - "com.innodox.docarch.api.exception.DocumentNotFoundException"
    item-related-exceptions:
      - "feign.FeignException$BadRequest"
      - "hu.perit.spvitamin.spring.exception.ResourceNotFoundException"
      - "hu.perit.spvitamin.spring.exception.InvalidInputException"
      - "com.innodox.docarch.api.exception.DocumentNotFoundException"
```

- `id` (long) — job type ID persisted in DB (stable identifier)
- `processor-class` (FQN) — your `AbstractProcessor` implementation
- `thread-pool-size` (int) — parallel workers for this job type
- `retry-timeout` (duration) — how long we will retry since creation time (e.g., `1h`, `24h`)
- `context-decorator-tag` (string, optional) — MDC/ThreadContext tag name for batch/job ids (default: `batchId`)
- `retryable-exceptions` (list of class names)
- `item-related-exceptions` (list of class names)

Notes:

- A class listed in `retryable-exceptions` makes errors retryable.
- A class listed in `item-related-exceptions` treats the error as item-specific (the batch continues).
- If an error is retryable AND not item-related, the batch is interrupted (infrastructure issue) but items remain
  `CREATED` to retry later.

---

## Implementing the persistence side

1) Entity implementing `ResilientJobData`

Schema and fields are under your control; here’s a simplified JPA entity:

```
@Getter
@Setter
@Entity
@Table(name = "TBL_GDPR_RESILIENT_JOB", indexes = {
        @Index(name = "ix_job_01", columnList = "creation_timestamp"),
        @Index(name = "ix_job_02", columnList = "status"),
        @Index(name = "ix_job_03", columnList = "processing_started_timestamp")
})
public class ResilientJobEntity implements ResilientJobData
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_generator")
    @SequenceGenerator(name = "job_generator", sequenceName = "seq_gdpr_resilient_job", allocationSize = 1)
    @NotNull
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "creation_timestamp")
    @Convert(converter = OffsetDateTimeToUTCConverter.class)
    private OffsetDateTime creationTimestamp;

    @NotNull
    @Column(name = "status", nullable = false)
    @Convert(converter = ResilientJobStatusConverter.class)
    private ResilientJobStatus status;

    @NotNull
    @Column(name = "processor", nullable = false)
    private Long processorType;

    @NotNull
    @Column(name = "parameter_version", nullable = false)
    private Integer parameterVersion;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @NotNull
    @Column(name = "parameters", nullable = false, columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "processing_started_timestamp")
    @Convert(converter = OffsetDateTimeToUTCConverter.class)
    private OffsetDateTime processingStartedTimestamp;

    @Column(name = "error_text", columnDefinition = "TEXT")
    private String errorText;

    @Column(name = "retry_count", nullable = false)
    private Long retryCount;


    public void setParameters(Object data)
    {
        try
        {
            ObjectMapper mapper = SpvitaminSpringObjectMapper.createMapper(SpvitaminObjectMapper.MapperType.JSON);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            this.parameters = mapper.writeValueAsString(data);
        }
        catch (JsonProcessingException e)
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
        catch (IOException e)
        {
            return ServerException.throwFrom(e);
        }
    }
}
```

2) Repository helpers (example snippets from `ResilientJobRepo`)

```
public interface ResilientJobRepo extends JpaRepository<ResilientJobEntity, Long>
{
    @Modifying
    @Query("update ResilientJobEntity e set e.status = :errorState where e.processorType = :processorType and e.status <> :errorState and e.creationTimestamp < :timestamp")
    int terminatePermanentlyFailingEntities(
            Long processorType,
            OffsetDateTime timestamp,
            ResilientJobStatus errorState
    );

    @Modifying
    @Query("update ResilientJobEntity e set e.status = :targetState, e.retryCount = e.retryCount + 1 where e.processorType = :processorType and e.status = :whereState and e.processingStartedTimestamp < :timestamp")
    int resetStuckInProgressEntities(
            Long processorType,
            OffsetDateTime timestamp,
            ResilientJobStatus whereState,
            ResilientJobStatus targetState
    );

    // We use here timeout = 0 which means, do not wait for locked rows.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
    List<ResilientJobEntity> findAllByProcessorTypeAndStatusOrderById(Long processorType, ResilientJobStatus status, PageRequest pageRequest);

    @Modifying
    @Query("update ResilientJobEntity e set e.status = :status, e.processingStartedTimestamp = :processingStartedTimestamp where e.id in :ids")
    int updateStatusAndProcessingStartedTimestamp(
            List<Long> ids,
            ResilientJobStatus status,
            OffsetDateTime processingStartedTimestamp
    );

    @Modifying
    @Query("update ResilientJobEntity e set e.status = :status, e.errorText = :errorText, e.retryCount = e.retryCount + 1 where e.id = :id")
    void updateStatusAndError(
            Long id,
            ResilientJobStatus status,
            String errorText
    ); 

    @Modifying
    @Query("update ResilientJobEntity e set e.status = :status where e.id in :ids and e.status = :criteria")
    int updateStatusWhere(List<Long> ids, ResilientJobStatus status, ResilientJobStatus criteria);
}
```

3) Create interface `ResilientJobEntityService`

```java
public interface ResilientJobEntityService extends ResilientJobDataService
{
    ResilientJobEntity save(ResilientJobEntity entity);
}
```

4) Implement `ResilientJobEntityService`

```java
@Service
@RequiredArgsConstructor
public class ResilientJobEntityServiceImpl implements ResilientJobEntityService
{
    public static final int MAX_CRITERIA_IN_QUERIES = 1000;

    private final ResilientJobRepo repo;


    @Override
    public ResilientJobEntity save(ResilientJobEntity entity)
    {
        return this.repo.save(entity);
    }


    @Override
    @Transactional
    public int terminatePermanentlyFailingEntities(ProcessorType processorType, Duration timeout)
    {
        return this.repo.terminatePermanentlyFailingEntities(processorType.getProcessorId(), OffsetDateTime.now().minusSeconds(timeout.getSeconds()), ResilientJobStatus.ERROR);
    }


    @Override
    @Transactional
    public int resetStuckInProgressEntities(ProcessorType processorType, Duration timeout)
    {
        return this.repo.resetStuckInProgressEntities(processorType.getProcessorId(), OffsetDateTime.now().minusSeconds(timeout.getSeconds()), ResilientJobStatus.IN_PROGRESS, ResilientJobStatus.CREATED);
    }


    @Override
    @Transactional
    public List<ResilientJobEntity> getNextBatchAndSetInProgressState(ProcessorType processorType)
    {
        PageRequest pageRequest = PageRequest.of(0, 200);
        List<ResilientJobEntity> entities = this.repo.findAllByProcessorTypeAndStatusOrderById(processorType.getProcessorId(), ResilientJobStatus.CREATED, pageRequest);
        this.repo.updateStatusAndProcessingStartedTimestamp(entities.stream().map(ResilientJobEntity::getId).toList(), ResilientJobStatus.IN_PROGRESS, OffsetDateTime.now());
        return entities;
    }


    @Override
    @Transactional
    public int resetInProgressEntitiesById(List<Long> ids)
    {
        // com.microsoft.sqlserver.jdbc.SQLServerException: The incoming request has too many parameters. The server supports a maximum of 2100 parameters. Reduce the number of parameters and resend the request
        return Lists.partition(ids, MAX_CRITERIA_IN_QUERIES).stream()
                .mapToInt(idList -> this.repo.updateStatusWhere(ids, ResilientJobStatus.CREATED, ResilientJobStatus.IN_PROGRESS))
                .sum();
    }


    @Override
    @Transactional
    public void deleteById(Long id)
    {
        this.repo.deleteById(id);
    }


    @Override
    @Transactional
    public void saveError(Long id, ResilientJobStatus resilientJobStatus, Exception e)
    {
        this.repo.updateStatusAndError(id, resilientJobStatus, StackTracer.toString(e));
    }
}
```

---

## Implementing a processor

Create a class extending `AbstractProcessor`. In GDPR service, `DocumentRemover` performs deletion in an external system
and updates a business log on success/error.

```
@Slf4j
public class DocumentRemover extends AbstractProcessor
{
    public DocumentRemover(ProcessorType processorType, ResilientJobProperties properties)
    {
        super(processorType, properties);
    }


    @Override
    public void processJob(ResilientJobData entity) throws Exception
    {
        RemoveJobRequest request = entity.getParameters(RemoveJobRequest.class);

        <put your business logic here>
    }


    @Override
    public void onError(ResilientJobData entity, Exception e)
    {
        RemoveJobRequest request = entity.getParameters(RemoveJobRequest.class);

        <put your business logic here>
    }
}
```

Register it in configuration under a name (e.g., `document-remover`) and assign a unique numeric `id` that you persist
in the `processorType` column.

---

## Enqueueing jobs

Create and save an entity row with required fields. In GDPR service:

```
@Service
public class ResilientJobRunnerServiceImpl implements ResilientJobRunnerService {
  private final ResilientJobEntityService jobService;
  private final ResilientJobCollectionProperties jobProps;

  @Override
  public ResilientJobEntity putRemoveJob(RemoveJobRequest request) {
    ResilientJobEntity e = new ResilientJobEntity();
    e.setCreationTimestamp(OffsetDateTime.now());
    e.setStatus(ResilientJobStatus.CREATED);
    e.setProcessorType(jobProps.get("document-remover").getId());
    e.setParameterVersion(1);
    e.setParameters(request); // serialized to JSON by entity
    e.setRetryCount(0L);
    return jobService.save(e);
  }
}
```

Your API endpoint or service layer calls the above to enqueue a job.

---

## How processing runs

`BJobProcessor` is a Spring `@Component` with a `@Scheduled(fixedDelay = 5000)` method. For each configured job type:

1) Terminates old jobs (older than `retry-timeout`) by setting them to `ERROR`.
2) Resets stale `IN_PROGRESS` jobs older than 5 minutes back to `CREATED` and increments `retryCount`.
3) Fetches the next page of `CREATED` jobs ordered by `id`, marks them `IN_PROGRESS` with a timestamp, and submits them
   to a thread pool.
4) Each individual job (`BJob`) calls your `AbstractProcessor.processJob` and, on success, deletes the row. On
   exceptions it classifies per configuration and either re-queues (`CREATED`) or marks permanent `ERROR` and invokes
   `onError`.

The runner ensures batch-level interruptions for infrastructure problems (retryable & not item-related) to avoid
exhausting a bad dependency while leaving items to be retried later.

---

## Exception classification cheat sheet

- Retryable exception? → job goes back to `CREATED` and will be tried again until `retry-timeout` expires.
- Item-related but not retryable? → job goes to `ERROR`, `onError` is invoked, batch continues.
- Retryable AND not item-related? → job re-queued (`CREATED`) AND the current batch is interrupted (likely infra
  outage) — processing resumes on next scheduler tick.
- Unknown exceptions are treated as retryable (not item-related) by default unless your configuration says otherwise.

You control classification by listing exception class names in config (`retryable-exceptions`,
`item-related-exceptions`). Matching is done by class name comparison against the thrown exception and its causes.

---

## Observability

- Logs include a thread context decorator tag (`context-decorator-tag`, default `batchId`) to correlate batch and job
  logs.
- `retryCount` is incremented when jobs are reset from `IN_PROGRESS` or when saving errors.
- `errorText` should contain the last error message/stack trace from `saveError` so you can diagnose failures from the
  DB.

---

## Tips and best practices

- Make `processJob` idempotent. Jobs may be retried or restarted after partial failures.
- Keep job parameters small but sufficient for idempotency (e.g., stable business keys). Store as JSON in `parameters`.
- Ensure proper DB indexes on `status`, `creationTimestamp`, and `processingStartedTimestamp` for efficient polling and
  resets.
- Use pessimistic locking when fetching batches to avoid concurrent runners picking the same rows.
- Carefully design exception lists. Overly broad retryable lists can mask item problems; overly strict item-related
  lists can prematurely mark `ERROR`.

---

## Troubleshooting

- Jobs never start: ensure `@EnableScheduling` is present and `resilient-jobs` config is loaded; verify your
  `ResilientJobDataService` bean is in the context.
- Jobs stuck in `IN_PROGRESS`: check that `resetStuckInProgressEntities` works and that your timestamps are saved in UTC
  as expected. Default reset is at 5 minutes.
- Re-tries don’t happen: check that exceptions you want to retry are listed in `retryable-exceptions` and that
  `saveError(id, CREATED, e)` stores the error and resets status to `CREATED`.
- Immediate permanent `ERROR`: verify whether the exception is categorized as item-related and not retryable; also
  ensure `retry-timeout` is not too small.

---

## Minimal checklist to integrate

- [ ] Add module dependency and enable scheduling
- [ ] Create entity implementing `ResilientJobData`
- [ ] Create repository and `ResilientJobDataService` implementation
- [ ] Implement `AbstractProcessor` for your job type(s)
- [ ] Configure `resilient-jobs.*` entries (id, processor class, exceptions, retry timeout)
- [ ] Enqueue jobs by inserting rows with `CREATED` status
- [ ] Monitor logs and DB for `ERROR` items

---

## License

Apache License, Version 2.0
