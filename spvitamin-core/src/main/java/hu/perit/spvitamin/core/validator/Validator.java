package hu.perit.spvitamin.core.validator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Validator<T>
{
    private final T subject;
    private final List<ValidationCriteria<T>> criteriaList;


    public static <T> AllBuilder<T> allBuilder(T subject)
    {
        return new AllBuilder<>(subject);
    }


    public static <T> AnyBuilder<T> anyBuilder(T subject)
    {
        return new AnyBuilder<>(subject);
    }


    public SuccessResult ensureAnyTrue(Boolean fastFail)
    {
        boolean success = false;
        List<String> successReasons = new ArrayList<>();
        for (ValidationCriteria<T> criteria : criteriaList)
        {
            if (criteria.succeeds(this.subject))
            {
                success = true;
                successReasons.add(criteria.getReason());
                if (BooleanUtils.isTrue(fastFail))
                {
                    break;
                }
            }
        }
        return new SuccessResult(success, successReasons);
    }


    public SuccessResult ensureAnyFalse(Boolean fastFail)
    {
        boolean success = false;
        List<String> successReasons = new ArrayList<>();
        for (ValidationCriteria<T> criteria : criteriaList)
        {
            if (criteria.fails(this.subject))
            {
                success = true;
                successReasons.add(criteria.getReason());
                if (BooleanUtils.isTrue(fastFail))
                {
                    break;
                }
            }
        }
        return new SuccessResult(success, successReasons);
    }


    public FailureResult ensureAllTrue(Boolean fastFail)
    {
        boolean success = true;
        List<String> failureReasons = new ArrayList<>();
        for (ValidationCriteria<T> criteria : criteriaList)
        {
            if (criteria.fails(this.subject))
            {
                success = false;
                failureReasons.add(criteria.getReason());
                if (BooleanUtils.isTrue(fastFail))
                {
                    break;
                }
            }
        }
        return new FailureResult(success, failureReasons);
    }


    public FailureResult ensureAllFalse(Boolean fastFail)
    {
        boolean success = true;
        List<String> failureReasons = new ArrayList<>();
        for (ValidationCriteria<T> criteria : criteriaList)
        {
            if (criteria.succeeds(this.subject))
            {
                success = false;
                failureReasons.add(criteria.getReason());
                if (BooleanUtils.isTrue(fastFail))
                {
                    break;
                }
            }
        }
        return new FailureResult(success, failureReasons);
    }


    public static class AllBuilder<T>
    {
        private final T subject;
        private final List<ValidationCriteria<T>> criteriaList = new ArrayList<>();
        private boolean fastFail = true;


        private AllBuilder(T subject)
        {
            this.subject = subject;
        }


        public AllBuilder<T> fastFail(Boolean fastFail)
        {
            this.fastFail = BooleanUtils.isTrue(fastFail);
            return this;
        }


        public AllBuilder<T> add(Predicate<T> predicate, String failureReason)
        {
            this.criteriaList.add(new ValidationCriteria<>(predicate, failureReason));
            return this;
        }


        public FailureResult ensureAllTrue()
        {
            return new Validator<>(this.subject, this.criteriaList).ensureAllTrue(fastFail);
        }


        public FailureResult ensureAllFalse()
        {
            return new Validator<>(this.subject, this.criteriaList).ensureAllFalse(fastFail);
        }


        public <E extends Exception> void ensureAllTrueOrElseThrow(ThrowingFunction<FailureResult, E> exceptionFactory) throws E
        {
            FailureResult result = ensureAllTrue();
            if (result.hasErrors())
            {
                throw exceptionFactory.apply(result);
            }
        }


        public <E extends Exception> void ensureAllFalseOrElseThrow(ThrowingFunction<FailureResult, E> exceptionFactory) throws E
        {
            FailureResult result = ensureAllFalse();
            if (result.hasErrors())
            {
                throw exceptionFactory.apply(result);
            }
        }
    }


    public static class AnyBuilder<T>
    {
        private final T subject;
        private final List<ValidationCriteria<T>> criteriaList = new ArrayList<>();
        private boolean fastFail = true;


        private AnyBuilder(T subject)
        {
            this.subject = subject;
        }


        public AnyBuilder<T> fastFail(Boolean fastFail)
        {
            this.fastFail = BooleanUtils.isTrue(fastFail);
            return this;
        }


        public AnyBuilder<T> add(Predicate<T> predicate, String successReason)
        {
            this.criteriaList.add(new ValidationCriteria<>(predicate, successReason));
            return this;
        }


        public SuccessResult ensureAnyTrue()
        {
            return new Validator<>(this.subject, this.criteriaList).ensureAnyTrue(fastFail);
        }


        public SuccessResult ensureAnyFalse()
        {
            return new Validator<>(this.subject, this.criteriaList).ensureAnyFalse(fastFail);
        }


        public <E extends Exception> void ensureAnyTrueOrElseThrow(ThrowingFunction<SuccessResult, E> exceptionFactory) throws E
        {
            SuccessResult result = ensureAnyTrue();
            if (result.hasErrors())
            {
                throw exceptionFactory.apply(result);
            }
        }


        public <E extends Exception> void ensureAnyFalseOrElseThrow(ThrowingFunction<SuccessResult, E> exceptionFactory) throws E
        {
            SuccessResult result = ensureAnyFalse();
            if (result.hasErrors())
            {
                throw exceptionFactory.apply(result);
            }
        }
    }
}
