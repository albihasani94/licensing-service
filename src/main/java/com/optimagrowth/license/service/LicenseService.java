package com.optimagrowth.license.service;

import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.repository.LicenseRepository;
import com.optimagrowth.license.util.ClientType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class LicenseService {

    private final MessageSource messageSource;
    private final LicenseRepository licenseRepository;
    private final ServiceConfig serviceConfig;
    private final OrganizationLookupService organizationLookupService;

    public LicenseService(MessageSource messageSource, LicenseRepository licenseRepository, ServiceConfig serviceConfig, OrganizationLookupService organizationLookupService) {
        this.messageSource = messageSource;
        this.licenseRepository = licenseRepository;
        this.serviceConfig = serviceConfig;
        this.organizationLookupService = organizationLookupService;
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

    public License getLicenseByClientType(Long licenseId, ClientType clientType) {
        License license = licenseRepository.findById(licenseId).orElseThrow(licenseNotFoundException(licenseId));

        var organization = organizationLookupService.getOrganization(license.getOrganizationId(), clientType);

        if (organization != null) {
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }

        return license;
    }
}
