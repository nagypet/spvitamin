/*
 * Copyright 2020-2025 the original author or authors.
 */

package hu.perit.spvitamin.core.saga;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Demonstrálja a SagaRunner használatát cég regisztráció + előfizetés létrehozás esetére.
 * <p>
 * Forgatókönyvek:
 * <ol>
 *   <li>Sikeres eset: mindkét lépés lefut, nincs kompenzáció</li>
 *   <li>Subscription hiba: a cég létrejött, de az előfizetés létrehozása hibásodik
 *       → a cég létrehozás kompenzálódik (törlés)</li>
 *   <li>Részbeni újraindítás: ha a cég már létrejött (isCompleted=true), a saga
 *       csak az előfizetést hozza létre</li>
 * </ol>
 */
@Slf4j
class SagaRunnerTest
{
    // -------------------------------------------------------------------------
    // Teszt domain modellek
    // -------------------------------------------------------------------------

    @Data
    static class RegistrationParam
    {
        private final String companyName;
        private final String subscriptionPlan;
    }

    @Data
    static class RegistrationContext
    {
        private Long companyId;          // null = cég még nem jött létre
        private Long subscriptionId;     // null = előfizetés még nem jött létre
    }

    // -------------------------------------------------------------------------
    // Fake "repository" és "kliens" — test double-ök, Mockito nélkül
    // -------------------------------------------------------------------------

    static class FakeCompanyRepo
    {
        private long sequence = 1L;
        final List<Long> savedIds = new ArrayList<>();
        final List<Long> deletedIds = new ArrayList<>();


        Long save(String name)
        {
            long id = sequence++;
            savedIds.add(id);
            log.info("FakeCompanyRepo: saved company '{}' with id={}", name, id);
            return id;
        }


        void delete(Long id)
        {
            deletedIds.add(id);
            log.info("FakeCompanyRepo: deleted company id={}", id);
        }
    }

    static class FakeSubscriptionClient
    {
        final List<Long> createdForCompanyIds = new ArrayList<>();
        boolean shouldFail = false;


        Long create(Long companyId, String plan)
        {
            if (shouldFail)
            {
                throw new RuntimeException("Subscription service unavailable");
            }
            createdForCompanyIds.add(companyId);
            log.info("FakeSubscriptionClient: created subscription for companyId={}, plan={}", companyId, plan);
            return companyId * 100L;
        }
    }

    // -------------------------------------------------------------------------
    // SagaStep implementációk
    // -------------------------------------------------------------------------

    static class CreateCompanyStep implements SagaStep<RegistrationParam, RegistrationContext>
    {
        private final FakeCompanyRepo repo;


        CreateCompanyStep(FakeCompanyRepo repo)
        {
            this.repo = repo;
        }


        @Override
        public boolean isCompleted(RegistrationContext ctx)
        {
            return ctx.getCompanyId() != null;
        }


        @Override
        public void execute(RegistrationParam param, RegistrationContext ctx)
        {
            ctx.setCompanyId(repo.save(param.getCompanyName()));
        }


        @Override
        public void compensate(RegistrationParam param, RegistrationContext ctx)
        {
            if (ctx.getCompanyId() != null)
            {
                repo.delete(ctx.getCompanyId());
                ctx.setCompanyId(null);
            }
        }
    }

    static class CreateSubscriptionStep implements SagaStep<RegistrationParam, RegistrationContext>
    {
        private final FakeSubscriptionClient client;


        CreateSubscriptionStep(FakeSubscriptionClient client)
        {
            this.client = client;
        }


        @Override
        public boolean isCompleted(RegistrationContext ctx)
        {
            return ctx.getSubscriptionId() != null;
        }


        @Override
        public void execute(RegistrationParam param, RegistrationContext ctx)
        {
            ctx.setSubscriptionId(client.create(ctx.getCompanyId(), param.getSubscriptionPlan()));
        }

        // compensate: no-op, az előfizetés nem jött létre (a hívás kivételt dobott)
    }

    // -------------------------------------------------------------------------
    // Tesztek
    // -------------------------------------------------------------------------


    @Test
    void successfulRegistration() throws Exception
    {
        FakeCompanyRepo companyRepo = new FakeCompanyRepo();
        FakeSubscriptionClient subscriptionClient = new FakeSubscriptionClient();

        SagaRunner<RegistrationParam, RegistrationContext> runner = new SagaRunner<>(List.of(
                new CreateCompanyStep(companyRepo),
                new CreateSubscriptionStep(subscriptionClient)
        ));

        RegistrationContext ctx = new RegistrationContext();
        runner.execute(new RegistrationParam("Acme Kft.", "PRO"), ctx);

        assertThat(ctx.getCompanyId()).isEqualTo(1L);
        assertThat(ctx.getSubscriptionId()).isEqualTo(100L);
        assertThat(companyRepo.savedIds).containsExactly(1L);
        assertThat(companyRepo.deletedIds).isEmpty();
        assertThat(subscriptionClient.createdForCompanyIds).containsExactly(1L);
    }


    @Test
    void subscriptionFailureTriggersCompanyCompensation()
    {
        FakeCompanyRepo companyRepo = new FakeCompanyRepo();
        FakeSubscriptionClient subscriptionClient = new FakeSubscriptionClient();
        subscriptionClient.shouldFail = true;

        SagaRunner<RegistrationParam, RegistrationContext> runner = new SagaRunner<>(List.of(
                new CreateCompanyStep(companyRepo),
                new CreateSubscriptionStep(subscriptionClient)
        ));

        RegistrationContext ctx = new RegistrationContext();

        assertThatThrownBy(() -> runner.execute(new RegistrationParam("Acme Kft.", "PRO"), ctx))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Subscription service unavailable");

        // A cég létrejött, de aztán kompenzálódott (törlésre került)
        assertThat(companyRepo.savedIds).containsExactly(1L);
        assertThat(companyRepo.deletedIds).containsExactly(1L);
        // Az előfizetés nem jött létre
        assertThat(subscriptionClient.createdForCompanyIds).isEmpty();
        // A context visszaállt
        assertThat(ctx.getCompanyId()).isNull();
        assertThat(ctx.getSubscriptionId()).isNull();
    }


    @Test
    void resumeSkipsAlreadyCompletedCompanyStep() throws Exception
    {
        FakeCompanyRepo companyRepo = new FakeCompanyRepo();
        FakeSubscriptionClient subscriptionClient = new FakeSubscriptionClient();

        SagaRunner<RegistrationParam, RegistrationContext> runner = new SagaRunner<>(List.of(
                new CreateCompanyStep(companyRepo),
                new CreateSubscriptionStep(subscriptionClient)
        ));

        // Szimuláljuk, hogy a cég már korábban létrejött (pl. perzisztált kontextusból betöltve)
        RegistrationContext ctx = new RegistrationContext();
        ctx.setCompanyId(42L);

        runner.execute(new RegistrationParam("Acme Kft.", "PRO"), ctx);

        // A cég létrehozása nem futott újra
        assertThat(companyRepo.savedIds).isEmpty();
        assertThat(companyRepo.deletedIds).isEmpty();
        // Az előfizetés létrejött a meglévő companyId alapján
        assertThat(ctx.getSubscriptionId()).isEqualTo(4200L);
        assertThat(subscriptionClient.createdForCompanyIds).containsExactly(42L);
    }
}
