package hu.perit.spvitamin.spring.logging;

import hu.perit.spvitamin.core.thing.PrinterVisitor;
import jakarta.xml.bind.JAXBElement;

public class RequestLoggerVisitor extends PrinterVisitor
{
    public RequestLoggerVisitor(Options options)
    {
        super(options);
    }


    @Override
    protected String convertProperty(String name, Object value)
    {
        if (value instanceof JAXBElement<?> jaxbElement && jaxbElement.getValue() instanceof byte[] bytes)
        {
            return formatByteArray(bytes);
        }

        return super.convertProperty(name, value);
    }
}
