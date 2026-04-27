package com.optimagrowth.license.config;

import com.optimagrowth.license.client.OrganizationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;
import java.util.Optional;

@Configuration
public class ClientConfig {

    @Value("${organization.base.url:http://organization-service}")
    private String organizationBaseUrl;

    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder(
            RestClientBuilderConfigurer configurer,
            ClientHttpRequestInterceptor bearerTokenRelayRestClientInterceptor) {
        return configurer.configure(RestClient.builder())
                .requestInterceptor(bearerTokenRelayRestClientInterceptor);
    }

    @Bean
    OrganizationClient organizationClient(RestClient.Builder restClientBuilder) {
        RestClient client = restClientBuilder
                .baseUrl(organizationBaseUrl)
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(OrganizationClient.class);
    }

    @Bean
    RestClient discoveryRestClient(
            @Value("${spring.http.client.connect-timeout:1s}") Duration connectTimeout,
            @Value("${spring.http.client.read-timeout:2s}") Duration readTimeout,
            ClientHttpRequestInterceptor bearerTokenRelayRestClientInterceptor) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .requestInterceptor(bearerTokenRelayRestClientInterceptor)
                .build();
    }

    @Bean
    ClientHttpRequestInterceptor bearerTokenRelayRestClientInterceptor() {
        return (request, body, execution) -> {
            currentBearerToken().ifPresent(request.getHeaders()::setBearerAuth);
            return execution.execute(request, body);
        };
    }

    private Optional<String> currentBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getCredentials() instanceof AbstractOAuth2Token token) {
            return Optional.of(token.getTokenValue());
        }

        return Optional.empty();
    }

}
