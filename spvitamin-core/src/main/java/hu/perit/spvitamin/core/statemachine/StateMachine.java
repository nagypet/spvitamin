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

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A generic implementation of a finite state machine (FSM).
 *
 * <p>This class provides a type-safe implementation of a finite state machine where
 * states and events are represented as enum values. It supports defining transitions
 * between states based on events, and provides methods to trigger state changes by
 * sending events to the machine.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Type-safe state and event definitions using enums</li>
 *   <li>Fluent API for configuring state transitions</li>
 *   <li>Thread-safe event processing</li>
 *   <li>Validation of event applicability in current state</li>
 *   <li>Detection of ambiguous transition definitions</li>
 *   <li>Support for ignored events: events that are silently accepted without causing a state transition</li>
 * </ul>
 *
 * <h2>Transitions vs. ignored events</h2>
 * <p>A <em>transition</em> (configured via {@code .on(event).to(target)}) causes the state machine to move
 * to a new state when the event is received. An <em>ignored event</em> (configured via {@code .ignore(event)})
 * is accepted by {@link #sendEvent} — which returns {@code true} — but does not change the current state.
 * Ignored events are intentionally excluded from {@link #isEventDefined}, which only returns {@code true}
 * for real transitions. Use {@link #isEventAllowed} to check whether an event is either a transition or
 * an ignored event for the current state.</p>
 *
 * <p>Ignored events are useful when a bulk operation must be applied to a collection of items that may
 * already be in the target state. For example, sending {@code APPROVE} to items where some are already
 * {@code APPROVED} would otherwise fail; declaring {@code .from(APPROVED).ignore(APPROVE)} makes the
 * operation idempotent.</p>
 *
 * <h2>Example usage</h2>
 * <pre>
 * // Define states and events as enums
 * enum State { IDLE, RUNNING, PAUSED, STOPPED }
 * enum Event { START, PAUSE, RESUME, STOP }
 *
 * // Create and configure the state machine
 * StateMachine&lt;State, Event&gt; machine = new StateMachine&lt;&gt;()
 *     .currentState(State.IDLE)
 *     .configureTransactions()
 *         .from(State.IDLE).on(Event.START).to(State.RUNNING)
 *         .from(State.RUNNING).on(Event.PAUSE).to(State.PAUSED)
 *                            .on(Event.STOP).to(State.STOPPED)
 *         .from(State.PAUSED).on(Event.RESUME).to(State.RUNNING)
 *                            .ignore(Event.PAUSE); // idempotent: already paused
 *
 * machine.sendEvent(Event.START);  // true  — State changes to RUNNING
 * machine.sendEvent(Event.PAUSE);  // true  — State changes to PAUSED
 * machine.sendEvent(Event.PAUSE);  // true  — ignored, state stays PAUSED
 * machine.sendEvent(Event.START);  // false — not defined in PAUSED
 * </pre>
 *
 * @param <S> the enum type representing states
 * @param <E> the enum type representing events
 */
@Slf4j
public class StateMachine<S extends Enum<?>, E extends Enum<?>>
{
    private List<Transaction<S, E>> transactions = new ArrayList<>();
    private S currentState;

    public TransactionConfigurer configureTransactions()
    {
        return new TransactionConfigurer();
    }

    public StateMachine<S, E> currentState(S state)
    {
        this.currentState = state;
        return this;
    }


    public S getCurrentState()
    {
        return this.currentState;
    }


    /**
     * Sends an event to the state machine.
     *
     * <p>If a matching transition is defined for the current state and the given event,
     * the machine moves to the target state and returns {@code true}.
     * If the event is configured as <em>ignored</em> for the current state (via {@code .ignore(event)}),
     * the machine stays in its current state and still returns {@code true}.
     * If no entry (transition or ignored) is found, {@code false} is returned.</p>
     *
     * @param event the event to send
     * @return {@code true} if the event was handled (transition or ignored), {@code false} if unknown
     */
    public synchronized boolean sendEvent(E event)
    {
        Optional<Transaction<S, E>> optTrx = getTransactionForEvent(event);
        if (optTrx.isEmpty())
        {
            log.debug("Event {} is not defined in state {}!", event, this.currentState);
            return false;
        }

        if (optTrx.get().isIgnored())
        {
            log.debug("Event {} is allowed but ignored in state {}!", event, this.currentState);
            return true;
        }

        this.currentState = optTrx.get().getTarget();
        return true;
    }


    /**
     * Returns {@code true} if the given event triggers a real state transition from the current state.
     *
     * <p>Ignored events (configured via {@code .ignore(event)}) are explicitly excluded:
     * this method returns {@code false} for them even though {@link #sendEvent} would accept them.
     * Use {@link #isEventAllowed} to include ignored events in the check.</p>
     *
     * @param event the event to check
     * @return {@code true} if a state-changing transition exists for the event in the current state
     */
    public synchronized boolean isEventDefined(E event)
    {
        Optional<Transaction<S, E>> optTrx = getTransactionForEvent(event);
        return optTrx.isPresent() && !optTrx.get().isIgnored();
    }


    /**
     * Returns {@code true} if the given event is accepted by the state machine in the current state,
     * regardless of whether it causes a state transition or is merely ignored.
     *
     * <p>This is equivalent to checking whether {@link #sendEvent} would return {@code true}
     * without actually sending the event.</p>
     *
     * @param event the event to check
     * @return {@code true} if the event is either a defined transition or an ignored event
     */
    public synchronized boolean isEventAllowed(E event)
    {
        Optional<Transaction<S, E>> optTrx = getTransactionForEvent(event);
        return optTrx.isPresent();
    }


    /**
     * Retrieves the set of events that are defined for transitions starting from the current state.
     * The defined events are extracted from the transitions that:
     * - originate from the current state,
     * - are not explicitly marked as ignored.
     *
     * @return a list containing the events that are allowed for transitions from the current state
     */
    public synchronized List<E> getDefinedEvents()
    {
        return this.transactions.stream()
                .filter(t -> t.getSource().equals(this.currentState))
                .filter(Transaction::isGuardMet)
                .filter(t -> !t.isIgnored())
                .map(t -> t.getEvent())
                .toList();
    }


    private synchronized Optional<Transaction<S, E>> getTransactionForEvent(E event)
    {
        List<Transaction<S, E>> trx = this.transactions.stream()
                .filter(t -> t.getSource().equals(this.currentState))
                .filter(t -> t.getEvent().equals(event))
                .filter(Transaction::isGuardMet)
                .toList();

        if (trx.isEmpty())
        {
            return Optional.empty();
        }

        if (trx.size() > 1)
        {
            throw new IllegalStateException(String.format("There are more than one matching transition from '%s' on '%s'! Check that guards are mutually exclusive.", this.currentState, event));
        }

        return Optional.ofNullable(trx.get(0));
    }


    protected void cloneFrom(StateMachine<S, E> stateMachine)
    {
        this.transactions = Collections.unmodifiableList(stateMachine.transactions);
        this.currentState = stateMachine.currentState;
    }


    // Returns source states where event is allowed
    public Set<S> getStateForEvent(E event)
    {
        return this.transactions.stream()
                .filter(t -> t.getEvent() == event && !t.isIgnored())
                .map(t -> t.getSource())
                .collect(Collectors.toSet());
    }


    //------------------------------------------------------------------------------------------------------------------
    // TransactionConfigurer
    //------------------------------------------------------------------------------------------------------------------
    public class TransactionConfigurer
    {
        private Transaction<S, E> current;

        public TransactionConfigurer add()
        {
            this.current = new Transaction<>();
            transactions.add(this.current);
            return this;
        }


        public TransactionConfigurer source(S source)
        {
            this.current.setSource(source);
            return this;
        }

        public TransactionConfigurer target(S target)
        {
            this.current.setTarget(target);
            return this;
        }

        public TransactionConfigurer event(E event)
        {
            this.current.setEvent(event);
            return this;
        }

        public SourceConfigurer from(S source)
        {
            return new SourceConfigurer(source);
        }


        //--------------------------------------------------------------------------------------------------------------
        // SourceConfigurer
        //--------------------------------------------------------------------------------------------------------------
        public class SourceConfigurer
        {
            private final S source;

            SourceConfigurer(S source)
            {
                this.source = source;
            }

            public TargetConfigurer on(E event)
            {
                Transaction<S, E> t = new Transaction<>();
                t.setSource(this.source);
                t.setEvent(event);
                transactions.add(t);
                return new TargetConfigurer(this, t);
            }

            /**
             * Declares that the given event is silently accepted (ignored) when the state machine
             * is in the configured source state, without causing any state transition.
             *
             * <p>{@link StateMachine#sendEvent} will return {@code true} for an ignored event,
             * while {@link StateMachine#isEventDefined} will still return {@code false} — only
             * {@link StateMachine#isEventAllowed} returns {@code true}.</p>
             *
             * <p>Typical use case: idempotent bulk operations where some items may already be
             * in the desired target state (e.g. {@code .from(APPROVED).ignore(APPROVE)}).</p>
             *
             * @param event the event to ignore in this source state
             * @return this {@code SourceConfigurer} for further chaining
             */
            public SourceConfigurer ignore(E event)
            {
                Transaction<S, E> t = new Transaction<>();
                t.setSource(this.source);
                t.setEvent(event);
                t.setIgnored(true);
                transactions.add(t);
                return this;
            }

            public SourceConfigurer from(S newSource)
            {
                return new SourceConfigurer(newSource);
            }
        }


        //--------------------------------------------------------------------------------------------------------------
        // TargetConfigurer
        //--------------------------------------------------------------------------------------------------------------
        public class TargetConfigurer
        {
            private final SourceConfigurer parent;
            private final Transaction<S, E> transaction;
            private Guard currentGuard;

            TargetConfigurer(SourceConfigurer parent, Transaction<S, E> transaction)
            {
                this.parent = parent;
                this.transaction = transaction;
            }

            /**
             * Attaches a guard condition to this transition. The transition is only taken when the guard returns {@code true}.
             *
             * <p>Multiple guards can be chained using {@link #and} and {@link #or}. Guards on the same
             * (source, event) pair must be mutually exclusive — an {@link IllegalStateException} is thrown
             * at runtime if more than one guard passes simultaneously.</p>
             *
             * @param guard the condition to evaluate before taking this transition
             * @return this configurer for further chaining
             */
            public TargetConfigurer when(Guard guard)
            {
                this.currentGuard = guard;
                this.transaction.setGuard(guard);
                return this;
            }

            /**
             * Combines the current guard with {@code other} using logical AND.
             *
             * @param other the additional condition
             * @return this configurer for further chaining
             */
            public TargetConfigurer and(Guard other)
            {
                Guard combined = this.currentGuard.and(other);
                this.currentGuard = combined;
                this.transaction.setGuard(combined);
                return this;
            }

            /**
             * Combines the current guard with {@code other} using logical OR.
             *
             * @param other the alternative condition
             * @return this configurer for further chaining
             */
            public TargetConfigurer or(Guard other)
            {
                Guard combined = this.currentGuard.or(other);
                this.currentGuard = combined;
                this.transaction.setGuard(combined);
                return this;
            }

            public SourceConfigurer to(S target)
            {
                this.transaction.setTarget(target);
                return this.parent;
            }
        }
    }
}
