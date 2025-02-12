package hu.perit.spvitamin.spring.feignclients;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.ArrayList;
import java.util.List;

public class RequestInterceptorAdapter implements RequestInterceptor
{
    private final List<RequestInterceptor> interceptors = new ArrayList<>();

    public void addInterceptor(RequestInterceptor interceptor)
    {
       this.interceptors.add(interceptor);
    }

    @Override
    public void apply(RequestTemplate requestTemplate)
    {
        this.interceptors.forEach(interceptor -> interceptor.apply(requestTemplate));
    }
}
