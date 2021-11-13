package hu.perit.spvitamin.spring.logging;

import java.util.Iterator;

public interface HttpWrapper
{
    Iterator<String> getHeaderNames();
    String getHeader(String name);
}
