package com.optimagrowth.license.controller;

import com.optimagrowth.license.model.License;
import com.optimagrowth.license.service.LicenseService;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("v1/organization/{organizationId}/license")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping("/{licenseId}")
    License getLicense(@PathVariable String organizationId, @PathVariable String licenseId) {
        return licenseService.getLicense(organizationId, licenseId);
    }

    @PutMapping
    String updateLicense(@PathVariable String organizationId, @RequestBody License license) {
        return licenseService.updateLicense(license, organizationId);
    }

    @PostMapping
    String createLicense(@PathVariable String organizationId, @RequestBody License license, @RequestHeader(value = "Accept-Language", required = false) Locale locale) {
        return licenseService.createLicense(license, organizationId, locale);
    }

    @DeleteMapping("/{licenseId}")
    String deleteLicense(@PathVariable String organizationId, @PathVariable String licenseId) {
        return licenseService.deleteLicense(licenseId, organizationId);
    }
}
