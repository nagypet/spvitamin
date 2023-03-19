/*
 * Copyright (c) 2023. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.core.exception;

@FunctionalInterface
public interface ThrowingRunnable
{
    void run() throws Exception;
}
