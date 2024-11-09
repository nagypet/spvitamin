package hu.perit.spvitamin.spring.logging;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.thing.PrinterVisitor;
import hu.perit.spvitamin.core.thing.Thing;
import hu.perit.spvitamin.spring.restmethodlogger.Arguments;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This logger can handle a REST or SOAP request object. Sensitive fields will not be logged, and byte[] will be logged
 * only with its size.
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class RequestLogger
{
    public static final String PASSWORD = "password";

    public static String toSubject(Object request)
    {
        try
        {
            Thing thing = Thing.from(request);
            RequestLoggerVisitor visitor = new RequestLoggerVisitor(PrinterVisitor.Options.builder().hidePasswords(true).ignoreNulls(true).build());
            thing.accept(null, visitor);
            return visitor.getJson();
        }
        catch (Exception e)
        {
            log.error(StackTracer.toString(e));
            return e.toString();
        }
    }


    public static String toSubject(Arguments arguments)
    {
        if (arguments.isEmpty())
        {
            return "";
        }

        try
        {
            Thing thing = Thing.from(arguments.getArgumentMap());
            RequestLoggerVisitor visitor = new RequestLoggerVisitor(PrinterVisitor.Options.builder().hidePasswords(true).ignoreNulls(true).build());
            thing.accept(null, visitor);
            return visitor.getJson();
        }
        catch (Exception e)
        {
            log.error(StackTracer.toString(e));
            return e.toString();
        }
    }
}
