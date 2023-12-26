/*
 * Copyright (c) 2022. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.core.statemachine;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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


    public synchronized boolean sendEvent(E event)
    {
        Optional<Transaction<S, E>> optTrx = getTransactionForEvent(event);
        if (optTrx.isEmpty())
        {
            log.debug("Event {} is not defined in state {}!", event, this.currentState);
            return false;
        }

        this.currentState = optTrx.get().getTarget();
        return true;
    }


    public synchronized boolean isEventDefined(E event)
    {
        Optional<Transaction<S, E>> optTrx = getTransactionForEvent(event);
        return optTrx.isPresent();
    }


    private synchronized Optional<Transaction<S, E>> getTransactionForEvent(E event)
    {
        List<Transaction<S, E>> trx = this.transactions.stream()
                .filter(t -> t.getSource().equals(this.currentState))
                .filter(t -> t.getEvent().equals(event))
                .collect(Collectors.toList());

        if (trx.isEmpty())
        {
            return Optional.empty();
        }

        if (trx.size() > 1)
        {
            throw new IllegalStateException(String.format("There are more then one transaction defined from '%s' on '%s'!", this.currentState, event));
        }

        return Optional.ofNullable(trx.get(0));
    }


    protected void cloneFrom(StateMachine<S, E> stateMachine)
    {
        this.transactions = Collections.unmodifiableList(stateMachine.transactions);
        this.currentState = stateMachine.currentState;
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
    }
}
