package com.optimagrowth.license.service;

import com.optimagrowth.license.model.License;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class LicenseService {

    private final Random random = new Random();

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

    public String createLicense(License license, String organizationId) {
        String responseMessage = null;
        if (license != null) {
            license.setOrganizationId(organizationId);
            responseMessage = "This is the post and the object is: %s".formatted(license);
        }
        return responseMessage;
    }

    public String updateLicense(License license, String organizationId) {
        String responseMessage = null;
        if (license != null) {
            license.setOrganizationId(organizationId);
            responseMessage = "This is the put and the object is: %s".formatted(license);
        }
        return responseMessage;
    }

    public String deleteLicense(String licenseId, String organizationId) {
        return "Deleting license with id %s for the organization %s".formatted(licenseId, organizationId);
    }
}
