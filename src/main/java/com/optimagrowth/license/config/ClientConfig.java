package com.optimagrowth.license.config;

import com.optimagrowth.license.client.OrganizationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class ClientConfig {

    @Value("${organization.base.url:http://organization-service}")
    private String organizationBaseUrl;

    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder(RestClientBuilderConfigurer configurer) {
        return configurer.configure(RestClient.builder());
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
            @Value("${spring.http.client.read-timeout:2s}") Duration readTimeout) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

}
