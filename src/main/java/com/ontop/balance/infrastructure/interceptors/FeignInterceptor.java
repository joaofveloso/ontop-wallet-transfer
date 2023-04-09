package com.ontop.balance.infrastructure.interceptors;

import feign.InvocationContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.ResponseInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignInterceptor implements RequestInterceptor, ResponseInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {

        log.debug("{} >>> {}", requestTemplate.feignTarget().name(), requestTemplate.path());
    }

    @Override
    public Object aroundDecode(InvocationContext invocationContext) {

        log.info("{} <<< {}", invocationContext.response().status(),
                invocationContext.response().body().toString());
        return invocationContext.proceed();
    }
}
