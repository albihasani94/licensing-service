package com.optimagrowth.license.service;

import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.LicenseRepository;
import com.optimagrowth.license.util.ClientType;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.optimagrowth.license.util.ClientType.DISCOVERY;
import static com.optimagrowth.license.util.ClientType.REST;

@Service
public class LicenseService {

    private final MessageSource messageSource;
    private final LicenseRepository licenseRepository;
    private final ServiceConfig serviceConfig;
    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;
    private final Map<ClientType, Function<Long, Organization>> clientTypeBasedFunctions;


    public LicenseService(MessageSource messageSource, LicenseRepository licenseRepository, ServiceConfig serviceConfig, DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder) {
        this.messageSource = messageSource;
        this.licenseRepository = licenseRepository;
        this.serviceConfig = serviceConfig;
        this.discoveryClient = discoveryClient;
        this.restClient = restClientBuilder.build();
        this.clientTypeBasedFunctions = Map.of(
                DISCOVERY, retrieveOrganizationInfoDiscovery(),
                REST, retrieveOrganizationInfoRest()
        );
    }

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

    public License getLicenseByClientType(Long licenseId, ClientType clientType) {
        License license = licenseRepository.findById(licenseId).orElseThrow(licenseNotFoundException(licenseId));

        Organization organization = clientTypeBasedFunctions.get(clientType).apply(license.getOrganizationId());

        if (organization != null) {
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }

        return license;
    }

    private Function<Long, Organization> retrieveOrganizationInfoDiscovery() {
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

    private Function<Long, Organization> retrieveOrganizationInfoRest() {
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
}
