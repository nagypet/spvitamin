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

package hu.perit.spvitamin.core.statemachine;

/**
 * A functional interface representing a boolean condition (guard) for state machine transitions.
 *
 * <p>Guards can be combined using {@link #and}, {@link #or}, and {@link #negate} to form
 * composite conditions without external utility methods:</p>
 *
 * <pre>
 * Guard approvalRequired = () -> settings.isApprovalEnabled();
 * Guard highValue         = () -> package.getAmount() > 1_000_000;
 *
 * .on(FINALIZE).when(approvalRequired).to(READY_FOR_APPROVAL)
 * .on(FINALIZE).when(approvalRequired.negate()).to(READY_FOR_PAYMENT)
 * .on(FINALIZE).when(approvalRequired.and(highValue)).to(NEEDS_EXTRA_APPROVAL)
 * </pre>
 */
@FunctionalInterface
public interface Guard
{
    boolean test();

    default Guard and(Guard other)
    {
        return () -> this.test() && other.test();
    }

    default Guard or(Guard other)
    {
        return () -> this.test() || other.test();
    }

    default Guard negate()
    {
        return () -> !this.test();
    }
}
