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

package hu.perit.spvitamin.spring.resilientjobrunner.privates;

import hu.perit.spvitamin.spring.resilientjobrunner.AbstractProcessor;
import hu.perit.spvitamin.spring.resilientjobrunner.ProcessorType;
import hu.perit.spvitamin.spring.resilientjobrunner.config.ResilientJobCollectionProperties;
import hu.perit.spvitamin.spring.resilientjobrunner.config.ResilientJobProperties;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobEntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BJobProcessorTest
{
    private ResilientJobCollectionProperties collectionProperties;
    private ResilientJobEntityService<?> resilientJobEntityService;


    @BeforeEach
    void setUpMocks()
    {
        collectionProperties = mock(ResilientJobCollectionProperties.class);
        resilientJobEntityService = mock(ResilientJobEntityService.class);
    }


    private static ResilientJobProperties createProps(String name, long id, Class<? extends AbstractProcessor> procClass)
    {
        ResilientJobProperties p = new ResilientJobProperties();
        p.setId(id);
        p.setProcessorClass(procClass.getName());
        p.setThreadPoolSize(1);
        p.setRetryTimeout(Duration.ofMinutes(30));
        p.setContextDecoratorTag("batchId");
        p.getRetryableExceptions().addAll(Arrays.asList(RuntimeException.class.getName()));
        p.getRetryableExceptions().addAll(Arrays.asList(IllegalArgumentException.class.getName()));
        return p;
    }


    private static ProcessorType pt(String name, long id)
    {
        return ProcessorType.of(name, id);
    }


    @Test
    void createProcessor_shouldInstantiateByReflection()
    {
        String name = "test";
        long id = 1L;
        ResilientJobProperties props = createProps(name, id, TestProcessor.class);
        BJobProcessor bJobProcessor = new BJobProcessor(collectionProperties, resilientJobEntityService);

        AbstractProcessor p = bJobProcessor.createProcessor(pt(name, id), props);

        assertThat(p).isInstanceOf(TestProcessor.class);
        assertThat(p.getProcessorType().getName()).isEqualTo(name);
        assertThat(p.getProcessorType().getProcessorId()).isEqualTo(id);
        assertThat(p.getProperties()).isEqualTo(props);
    }


    @Test
    void process_shouldCallServicesAndReturn_onEmptyBatch()
    {
        String name = "job1";
        long id = 11L;
        ResilientJobProperties props = createProps(name, id, TestProcessor.class);

        Map<String, ResilientJobProperties> map = new HashMap<>();
        map.put(name, props);
        when(collectionProperties.getResilientJobs()).thenReturn(map);
        when(collectionProperties.get(name)).thenReturn(props);

        when(resilientJobEntityService.resetStuckInProgressEntities(any(), any())).thenReturn(0);
        when(resilientJobEntityService.getNextBatchAndSetInProgressState(any(), anyLong())).thenReturn(java.util.Collections.emptyList());

        BJobProcessor processor = new BJobProcessor(collectionProperties, resilientJobEntityService);
        processor.setUp();

        ProcessorType processorType = pt(name, id);
        processor.process(processorType);

        // verify initial calls
        verify(resilientJobEntityService, times(1)).resetStuckInProgressEntities(eq(processorType), eq(Duration.ofMinutes(5)));

        ArgumentCaptor<Long> lastIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(resilientJobEntityService, atLeastOnce()).getNextBatchAndSetInProgressState(eq(processorType), lastIdCaptor.capture());
        assertThat(lastIdCaptor.getValue()).isEqualTo(0L);

        // Since the batch was empty initially, there should be no final reset (the finally is inside the loop)
        verify(resilientJobEntityService, never()).resetStuckInProgressEntities(eq(processorType), eq(Duration.ofMinutes(0)));
    }


    @Test
    void setUp_shouldCreateExecutorForEachConfiguredJob() throws Exception
    {
        ResilientJobProperties p1 = createProps("jobA", 1L, TestProcessor.class);
        ResilientJobProperties p2 = createProps("jobB", 2L, TestProcessor.class);

        Map<String, ResilientJobProperties> map = new HashMap<>();
        map.put("jobA", p1);
        map.put("jobB", p2);

        when(collectionProperties.getResilientJobs()).thenReturn(map);

        BJobProcessor processor = new BJobProcessor(collectionProperties, resilientJobEntityService);
        processor.setUp();

        Field f = BJobProcessor.class.getDeclaredField("executorMap");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<ProcessorType, BatchExecutor> execMap = (Map<ProcessorType, BatchExecutor>) f.get(processor);

        assertThat(execMap).hasSize(2);
        assertThat(execMap.keySet().stream().map(ProcessorType::getName).toList())
                .containsExactlyInAnyOrder("jobA", "jobB");
    }

    // --------------------------------------------------------------------------------------------
    // Test helpers
    // --------------------------------------------------------------------------------------------

    static class TestProcessor extends AbstractProcessor
    {
        public TestProcessor(ProcessorType processorType, ResilientJobProperties properties)
        {
            super(processorType, properties);
        }


        @Override
        public void processJob(AbstractResilientJobEntity entity)
        {
            // no-op
        }


        @Override
        public void onError(AbstractResilientJobEntity entity, Exception e)
        {
            // no-op
        }
    }

    static class TestResilientJobEntity extends AbstractResilientJobEntity
    {
        private final Long id;
        private final OffsetDateTime creationTimestamp;
        private final Long retryCount;


        TestResilientJobEntity(Long id, OffsetDateTime creationTimestamp, Long retryCount)
        {
            this.id = id;
            this.creationTimestamp = creationTimestamp;
            this.retryCount = retryCount;
        }


        @Override
        public Long getId()
        {
            return id;
        }


        @Override
        public OffsetDateTime getCreationTimestamp()
        {
            return creationTimestamp;
        }


        @Override
        public hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatus getStatus()
        {
            return null;
        }


        @Override
        public Long getProcessorType()
        {
            return 0L;
        }


        @Override
        public Integer getParameterVersion()
        {
            return 1;
        }


        @Override
        public OffsetDateTime getProcessingFirstStartedTimestamp()
        {
            return null;
        }


        @Override
        public OffsetDateTime getProcessingLastStartedTimestamp()
        {
            return null;
        }


        @Override
        public String getErrorText()
        {
            return null;
        }


        @Override
        public Long getRetryCount()
        {
            return retryCount;
        }


        @Override
        public String getParameters()
        {
            return null;
        }
    }
}
