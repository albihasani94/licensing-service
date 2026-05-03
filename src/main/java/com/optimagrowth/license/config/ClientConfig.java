package com.optimagrowth.license.config;

import com.optimagrowth.license.client.OrganizationClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class ClientConfig {

    public static final String ORGANIZATION_REST_CLIENT_BUILDER = "organizationRestClientBuilder";

    @Value("${organization.base.url:http://organization-service}")
    private String organizationBaseUrl;

    @Bean(defaultCandidate = false)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @LoadBalanced
    RestClient.Builder organizationRestClientBuilder(
            RestClient.Builder restClientBuilder,
            ClientHttpRequestInterceptor bearerTokenRelayRestClientInterceptor) {
        return restClientBuilder
                .requestInterceptor(bearerTokenRelayRestClientInterceptor);
    }

    @Bean
    OrganizationClient organizationClient(
            @Qualifier(ORGANIZATION_REST_CLIENT_BUILDER) RestClient.Builder restClientBuilder) {
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
    BearerTokenResolver bearerTokenResolver() {
        return new BearerTokenResolver();
    }

    @Bean
    ClientHttpRequestInterceptor bearerTokenRelayRestClientInterceptor(BearerTokenResolver bearerTokenResolver) {
        return (request, body, execution) -> {
            bearerTokenResolver.currentBearerToken().ifPresent(request.getHeaders()::setBearerAuth);
            return execution.execute(request, body);
        };
    }

}
