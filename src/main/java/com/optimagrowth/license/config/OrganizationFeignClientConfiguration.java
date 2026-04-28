package com.optimagrowth.license.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

public class OrganizationFeignClientConfiguration {

    @Bean
    RequestInterceptor organizationFeignBearerTokenRelayInterceptor(BearerTokenResolver bearerTokenResolver) {
        return template -> bearerTokenResolver.currentBearerToken()
                .ifPresent(token -> {
                    template.removeHeader(HttpHeaders.AUTHORIZATION);
                    template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                });
    }

}
