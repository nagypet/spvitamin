package hu.perit.spvitamin.core.validator;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ValidatorTest
{
    @Data
    private static class TestEntity
    {
        private final Long id;
        private final String name;
    }


    @Test
    void test()
    {
        TestEntity entity = new TestEntity(1L, "name");
        assertThat(evaluate(entity).isSuccess()).isTrue();
        assertThat(evaluateAll(entity).isSuccess()).isTrue();
        assertThat(evaluateAny(entity).isSuccess()).isTrue();
    }


    @Test
    void test2()
    {
        TestEntity entity = new TestEntity(1L, null);
        assertThat(evaluate(entity).hasErrors()).isTrue();
        assertThat(evaluateAll(entity).hasErrors()).isTrue();
        assertThat(evaluateAny(entity).isSuccess()).isTrue();
    }


    @Test
    void test3()
    {
        TestEntity entity = new TestEntity(null, null);
        assertThat(evaluate(entity).hasErrors()).isTrue();
        assertThat(evaluateAll(entity).hasErrors()).isTrue();
        assertThat(evaluateAny(entity).isSuccess()).isFalse();
    }


    private static FailureResult evaluate(TestEntity entity)
    {
        FailureResult result = Validator.allBuilder(entity)
                .add(subject -> subject.getName() != null, "name is missing")
                .add(subject -> subject.getId() != null, "id is missing")
                .ensureAllTrue();

        if (result.hasErrors())
        {
            log.debug("Evaluation failed because: " + result.getFailureReasons());
        }
        return result;
    }


    private static FailureResult evaluateAll(TestEntity entity)
    {
        FailureResult result = Validator.allBuilder(entity)
                .fastFail(false)
                .add(subject -> subject.getName() != null, "name is missing")
                .add(subject -> subject.getId() != null, "id is missing")
                .ensureAllTrue();

        if (result.hasErrors())
        {
            log.debug("Evaluation of all criteria failed because: " + result.getFailureReasons());
        }
        return result;
    }


    private static SuccessResult evaluateAny(TestEntity entity)
    {
        SuccessResult result = Validator.anyBuilder(entity)
                .add(subject -> subject.getName() != null, "name is OK")
                .add(subject -> subject.getId() != null, "id is OK")
                .ensureAnyTrue();

        if (result.isSuccess())
        {
            log.debug("Evaluation of any criteria succeeded because: " + result.getSuccessReasons());
        }
        return result;
    }
}
