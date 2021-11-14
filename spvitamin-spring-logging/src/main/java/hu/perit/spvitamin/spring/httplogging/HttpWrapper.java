package hu.perit.spvitamin.spring.httplogging;

import java.util.Iterator;

interface HttpWrapper
{
    Iterator<String> getHeaderNames();
    String getHeader(String name);
}
