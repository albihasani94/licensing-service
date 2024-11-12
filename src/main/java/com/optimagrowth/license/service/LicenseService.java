package com.optimagrowth.license.service;

import com.optimagrowth.license.model.License;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Random;

@Service
public class LicenseService {

    private final MessageSource messageSource;

    private final Random random = new Random();

    public LicenseService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public License getLicense(String licenseId, String organizationId) {
        License license = new License();
        license.setId(random.nextInt(1000));
        license.setLicenseId(licenseId);
        license.setOrganizationId(organizationId);
        license.setDescription("Software product");
        license.setProductName("OStock");
        license.setLicenseType("full");

        return license;
    }

    public String createLicense(License license, String organizationId, Locale locale) {
        String responseMessage = null;
        if (license != null) {
            license.setOrganizationId(organizationId);
            responseMessage = messageSource.getMessage("license.create.message", null, locale).formatted(license);
        }
        return responseMessage;
    }

    public String updateLicense(License license, String organizationId) {
        String responseMessage = null;
        if (license != null) {
            license.setOrganizationId(organizationId);
            responseMessage = messageSource.getMessage("license.update.message", null, null).formatted(license);
        }
        return responseMessage;
    }

    public String deleteLicense(String licenseId, String organizationId) {
        return "Deleting license with id %s for the organization %s".formatted(licenseId, organizationId);
    }
}
