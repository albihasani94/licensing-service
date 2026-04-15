package com.optimagrowth.license.service;

import com.optimagrowth.license.client.OrganizationFeignClient;
import com.optimagrowth.license.client.OrganizationClient;
import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.LicenseRepository;
import com.optimagrowth.license.util.ClientType;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.LongFunction;
import java.util.function.Supplier;

@Service
public class LicenseService {

    private static final Logger LOG = LoggerFactory.getLogger(LicenseService.class);

    private final MessageSource messageSource;
    private final LicenseRepository licenseRepository;
    private final ServiceConfig serviceConfig;
    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;
    private final OrganizationFeignClient organizationFeignClient;
    private final OrganizationClient organizationClient;

    public LicenseService(MessageSource messageSource, LicenseRepository licenseRepository, ServiceConfig serviceConfig, DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder, OrganizationFeignClient organizationFeignClient, OrganizationClient organizationClient) {
        this.messageSource = messageSource;
        this.licenseRepository = licenseRepository;
        this.serviceConfig = serviceConfig;
        this.discoveryClient = discoveryClient;
        this.restClient = restClientBuilder.build();
        this.organizationFeignClient = organizationFeignClient;
        this.organizationClient = organizationClient;
    }

    @CircuitBreaker(name = "licenseById")
    public License getLicense(Long licenseId) {
        return licenseRepository.findById(licenseId)
                .map(license -> license.withComment(serviceConfig.getProperty()))
                .orElseThrow(licenseNotFoundException(licenseId));
    }

    private Supplier<IllegalArgumentException> licenseNotFoundException(Long licenseId) {
        return () -> new IllegalArgumentException(String.format(messageSource.getMessage("license.search.error.message", null, null), licenseId));
    }

    public License createOrUpdate(License license) {
        licenseRepository.save(license);
        return license.withComment(serviceConfig.getProperty());
    }

    public String deleteLicense(Long licenseId) {
        licenseRepository.deleteById(licenseId);
        return String.format(messageSource.getMessage("license.delete.message", null, null), licenseId);

    }

    @CircuitBreaker(name = "licenseByClientType", fallbackMethod = "licenseByClientTypeFallback")
    @Bulkhead(name = "bulkheadLicenseByClientType", fallbackMethod = "licenseByClientTypeFallback")
    @Retry(name = "retryLicenseByClientType", fallbackMethod = "licenseByClientTypeFallback")
    @RateLimiter(name = "licenseByClientType", fallbackMethod = "licenseByClientTypeFallback")
    public License getLicenseByClientType(Long licenseId, ClientType clientType) {
        License license = licenseRepository.findById(licenseId).orElseThrow(licenseNotFoundException(licenseId));

        var client = switch (clientType) {
            case DISCOVERY -> retrieveOrganizationInfoDiscovery();
            case REST -> retrieveOrganizationInfoRest();
            case FEIGN -> retrieveOrganizationInfoFeign();
            case SPRING -> retrieveOrganizationInfoSpringInterface();
        };

        var organization = client.apply(license.getOrganizationId());

        if (organization != null) {
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }

        return license;
    }

    private License licenseByClientTypeFallback(Long licenseId, ClientType clientType, Throwable throwable) {
        LOG.info("Something went wrong, reverting to fallback.");
        LOG.debug("Fallback caused by: ", throwable);
        return licenseRepository.findById(licenseId).orElseThrow(licenseNotFoundException(licenseId));
    }

    private LongFunction<Organization> retrieveOrganizationInfoDiscovery() {
        return organizationId -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("organization-service");

            if (instances.isEmpty()) {
                return null;
            }

            String serviceUri = "%s/v1/organization/%s".formatted(instances.getFirst().getUri().toString(), organizationId);

            ResponseEntity<Organization> restExchange = RestClient.builder().build()
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
