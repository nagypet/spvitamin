package hu.perit.spvitamin.spring.email.imap.service;

@FunctionalInterface
interface RetryableOperation<T>
{
    T execute();
}
