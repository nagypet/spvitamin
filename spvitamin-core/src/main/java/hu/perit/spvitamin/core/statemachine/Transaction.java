/*
 * Copyright (c) 2022. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.core.statemachine;

import lombok.Data;

@Data
public class Transaction<S extends Enum<?>, E extends Enum<?>>
{
    private S source;
    private S target;
    private E event;
}
