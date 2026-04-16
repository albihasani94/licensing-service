package com.optimagrowth.license.service;

import com.optimagrowth.license.client.OrganizationClient;
import com.optimagrowth.license.client.OrganizationFeignClient;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.util.ClientType;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.LongFunction;

@Service
public class OrganizationLookupService {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationLookupService.class);

    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;
    private final OrganizationFeignClient organizationFeignClient;
    private final OrganizationClient organizationClient;
    private final RestClient discoveryRestClient;

    public OrganizationLookupService(DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder, OrganizationFeignClient organizationFeignClient, OrganizationClient organizationClient, RestClient discoveryRestClient) {
        this.discoveryClient = discoveryClient;
        this.restClient = restClientBuilder.build();
        this.organizationFeignClient = organizationFeignClient;
        this.organizationClient = organizationClient;
        this.discoveryRestClient = discoveryRestClient;
    }

    @CircuitBreaker(name = "licenseByClientType", fallbackMethod = "organizationLookupFallback")
    @Bulkhead(name = "bulkheadLicenseByClientType", fallbackMethod = "organizationLookupFallback")
    @Retry(name = "retryLicenseByClientType", fallbackMethod = "organizationLookupFallback")
    @RateLimiter(name = "licenseByClientType", fallbackMethod = "organizationLookupFallback")
    public Organization getOrganization(Long organizationId, ClientType clientType) {
        var client = switch (clientType) {
            case DISCOVERY -> retrieveOrganizationInfoDiscovery();
            case REST -> retrieveOrganizationInfoRest();
            case FEIGN -> retrieveOrganizationInfoFeign();
            case SPRING -> retrieveOrganizationInfoSpringInterface();
        };

        return client.apply(organizationId);
    }

    private Organization organizationLookupFallback(Long organizationId, ClientType clientType, Throwable throwable) {
        LOG.info("Organization lookup failed through {}, returning license without organization details.", clientType);
        LOG.debug("Organization lookup fallback caused by: ", throwable);
        return null;
    }

    private LongFunction<Organization> retrieveOrganizationInfoDiscovery() {
        return organizationId -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("organization-service");

            if (instances.isEmpty()) {
                return null;
            }

            String serviceUri = "%s/v1/organization/%s".formatted(instances.getFirst().getUri().toString(), organizationId);

            ResponseEntity<Organization> restExchange = discoveryRestClient
                    .get()
                    .uri(serviceUri)
                    .retrieve()
                    .toEntity(Organization.class);

            return restExchange.getBody();
        };
    }

    private LongFunction<Organization> retrieveOrganizationInfoRest() {
        return organizationId -> {
            String serviceUri = "http://organization-service/v1/organization/" + organizationId;

            ResponseEntity<Organization> restExchange = restClient
                    .get()
                    .uri(serviceUri)
                    .retrieve()
                    .toEntity(Organization.class);

            return restExchange.getBody();
        };
    }

    private LongFunction<Organization> retrieveOrganizationInfoFeign() {
        return organizationFeignClient::getOrganization;
    }

    private LongFunction<Organization> retrieveOrganizationInfoSpringInterface() {
        return organizationClient::getOrganization;
    }
}
