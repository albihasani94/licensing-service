package com.optimagrowth.license.controller;

import com.optimagrowth.license.model.License;
import com.optimagrowth.license.service.LicenseService;
import com.optimagrowth.license.util.ClientType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("v1/organization/{organizationId}/license")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping("/{licenseId}")
    @PreAuthorize("hasAnyRole('user', 'admin')")
    public ResponseEntity<License> getLicense(@PathVariable Long licenseId) {
        var license = licenseService.getLicense(licenseId);

        license.add(linkTo(methodOn(LicenseController.class)
                        .getLicense(license.getLicenseId()))
                        .withSelfRel(),
                linkTo(methodOn(LicenseController.class)
                        .createLicense(license))
                        .withRel("createLicense"),
                linkTo(methodOn(LicenseController.class)
                        .updateLicense(license))
                        .withRel("updateLicense"),
                linkTo(methodOn(LicenseController.class)
                        .deleteLicense(licenseId))
                        .withRel("deleteLicense"));

        return ResponseEntity.ok(license);
    }

    @GetMapping("/{licenseId}/{clientType}")
    @PreAuthorize("hasAnyRole('user', 'admin')")
    public License getLicenseWithClientType(@PathVariable Long licenseId, @PathVariable ClientType clientType) {
        return licenseService.getLicenseByClientType(licenseId, clientType);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('user', 'admin')")
    public ResponseEntity<License> updateLicense(@RequestBody License license) {
        return ResponseEntity.ok(licenseService.createOrUpdate(license));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('user', 'admin')")
    public ResponseEntity<License> createLicense(@RequestBody License license) {
        return ResponseEntity.ok(licenseService.createOrUpdate(license));
    }

    @DeleteMapping(value = "/{licenseId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> deleteLicense(@PathVariable("licenseId") Long licenseId) {
        return ResponseEntity.ok(licenseService.deleteLicense(licenseId));
    }
}
