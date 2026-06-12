package hu.perit.spvitamin.spring.resilientjobrunner.saga;

import hu.perit.spvitamin.core.saga.SagaStep;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SagaExecutorTest
{
    // -------------------------------------------------------------------------
    // Test doubles
    // -------------------------------------------------------------------------

    static class StubEntity extends AbstractResilientJobEntity {}

    static class StubParam extends ResilientJobParameter
    {
        @Override
        public int getVersion() { return 1; }

        @Override
        protected Class<? extends ResilientJobParameter> getPreviousVersionClass() { return null; }

        @Override
        protected ResilientJobParameter migrateFrom(ResilientJobParameter previous) { return null; }
    }

    static class StubContext extends ResilientJobParameter
    {
        @Override
        public int getVersion() { return 1; }

        @Override
        protected Class<? extends ResilientJobParameter> getPreviousVersionClass() { return null; }

        @Override
        protected ResilientJobParameter migrateFrom(ResilientJobParameter previous) { return null; }
    }

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private SagaDefinition<StubParam, StubContext> sagaDef;
    private SagaStep<StubParam, StubContext> step1;
    private SagaStep<StubParam, StubContext> step2;
    private SagaDefinitionRegistry registry;
    private SagaExecutor executor;
    private StubEntity job;
    private StubParam param;
    private StubContext context;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp()
    {
        sagaDef = mock(SagaDefinition.class);
        step1 = mock(SagaStep.class);
        step2 = mock(SagaStep.class);
        registry = mock(SagaDefinitionRegistry.class);

        doReturn(sagaDef).when(registry).get(42L);

        param = new StubParam();
        context = new StubContext();
        when(sagaDef.loadParameter(any(), anyInt())).thenReturn(param);
        when(sagaDef.loadContext(any(), anyInt())).thenReturn(context);

        executor = new SagaExecutor(registry);

        job = new StubEntity();
        job.setId(1L);
        job.setProcessorType(42L);
        job.setParameters("{}");
        job.setParameterVersion(1);
        job.setSagaContext("{}");
        job.setSagaContextVersion(1);
        job.setParameterHash("hash");
        job.setStatus(null);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * Alap eset: egyetlen lépés, reconcile=false -> execute és persistContext meghívódik.
     */
    @Test
    void execute_singleStep_executeAndPersistCalled() throws Exception
    {
        when(sagaDef.steps()).thenReturn(List.of(step1));
        when(step1.isCompleted(context)).thenReturn(false);
        when(step1.reconcile(param, context)).thenReturn(false);

        executor.execute(job);

        verify(step1).execute(param, context);
        verify(sagaDef).persistContext(eq(1L), eq(context), anyString());
    }


    /**
     * Ha a lépés már kész (isCompleted=true), sem reconcile, sem execute, sem persistContext nem hívódik.
     */
    @Test
    void execute_completedStepIsSkipped() throws Exception
    {
        when(sagaDef.steps()).thenReturn(List.of(step1));
        when(step1.isCompleted(context)).thenReturn(true);

        executor.execute(job);

        verify(step1, never()).reconcile(any(), any());
        verify(step1, never()).execute(any(), any());
        verify(sagaDef, never()).persistContext(any(), any(), anyString());
    }


    /**
     * Ha reconcile=true (mellékhatás már megtörtént), az execute kihagyódik,
     * de persistContext meghívódik (a reconcile által frissített context mentéséhez).
     */
    @Test
    void execute_reconcileTrue_executeSkippedButContextPersisted() throws Exception
    {
        when(sagaDef.steps()).thenReturn(List.of(step1));
        when(step1.isCompleted(context)).thenReturn(false);
        when(step1.reconcile(param, context)).thenReturn(true);

        executor.execute(job);

        verify(step1, never()).execute(any(), any());
        verify(sagaDef).persistContext(eq(1L), eq(context), anyString());
    }


    /**
     * Két lépésnél mindkettő execute-ja lefut, és mindkettő után meghívódik a persistContext –
     * a megfelelő sorrendben.
     */
    @Test
    void execute_twoSteps_persistCalledAfterEachStep() throws Exception
    {
        when(sagaDef.steps()).thenReturn(List.of(step1, step2));
        when(step1.isCompleted(context)).thenReturn(false);
        when(step2.isCompleted(context)).thenReturn(false);
        when(step1.reconcile(param, context)).thenReturn(false);
        when(step2.reconcile(param, context)).thenReturn(false);

        executor.execute(job);

        verify(sagaDef, times(2)).persistContext(eq(1L), eq(context), anyString());

        InOrder order = inOrder(step1, sagaDef, step2);
        order.verify(step1).execute(param, context);
        order.verify(sagaDef).persistContext(eq(1L), eq(context), anyString());
        order.verify(step2).execute(param, context);
        order.verify(sagaDef).persistContext(eq(1L), eq(context), anyString());
    }


    /**
     * Ha az execute kivételt dob, az felszáll, és az adott lépéshez persistContext nem hívódik.
     */
    @Test
    void execute_stepThrows_exceptionPropagatedAndContextNotPersisted() throws Exception
    {
        when(sagaDef.steps()).thenReturn(List.of(step1));
        when(step1.isCompleted(context)).thenReturn(false);
        when(step1.reconcile(param, context)).thenReturn(false);
        doThrow(new RuntimeException("step failed")).when(step1).execute(param, context);

        assertThatThrownBy(() -> executor.execute(job))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("step failed");

        verify(sagaDef, never()).persistContext(any(), any(), anyString());
    }


    /**
     * Ismeretlen processorType esetén IllegalStateException dobódik.
     */
    @Test
    void execute_unknownProcessorType_throwsIllegalStateException()
    {
        doThrow(new IllegalStateException("No saga definition for processor 99")).when(registry).get(99L);
        job.setProcessorType(99L);

        assertThatThrownBy(() -> executor.execute(job))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("99");
    }


    /**
     * Üres lépéslistánál nincs execute és nincs persistContext hívás.
     */
    @Test
    void execute_emptyStepList_noPersistCalled() throws Exception
    {
        when(sagaDef.steps()).thenReturn(List.of());

        executor.execute(job);

        verify(sagaDef, never()).persistContext(any(), any(), anyString());
    }


    /**
     * Ha az első lépés kész, csak a második lépés execute-ja fut le, és egyszer hívódik persistContext.
     */
    @Test
    void execute_firstCompletedSecondNot_onlySecondStepExecuted() throws Exception
    {
        when(sagaDef.steps()).thenReturn(List.of(step1, step2));
        when(step1.isCompleted(context)).thenReturn(true);
        when(step2.isCompleted(context)).thenReturn(false);
        when(step2.reconcile(param, context)).thenReturn(false);

        executor.execute(job);

        verify(step1, never()).execute(any(), any());
        verify(step2).execute(param, context);
        verify(sagaDef, times(1)).persistContext(eq(1L), eq(context), anyString());
    }


    /**
     * Ha a context és a paraméter betöltése a megfelelő JSON-nel és verzióval történik.
     */
    @Test
    void execute_loadsContextAndParameterWithCorrectVersions() throws Exception
    {
        job.setSagaContext("{\"state\":\"init\"}");
        job.setSagaContextVersion(3);
        job.setParameters("{\"id\":7}");
        job.setParameterVersion(2);

        when(sagaDef.steps()).thenReturn(List.of());

        executor.execute(job);

        verify(sagaDef).loadContext("{\"state\":\"init\"}", 3);
        verify(sagaDef).loadParameter("{\"id\":7}", 2);
    }
}
