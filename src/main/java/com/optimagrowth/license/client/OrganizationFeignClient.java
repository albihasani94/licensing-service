package com.optimagrowth.license.client;

import com.optimagrowth.license.config.OrganizationFeignClientConfiguration;
import com.optimagrowth.license.model.Organization;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "organization-service", configuration = OrganizationFeignClientConfiguration.class)
public interface OrganizationFeignClient {

    @GetMapping(value = "/v1/organization/{organizationId}", consumes = "application/json")
    Organization getOrganization(@PathVariable("organizationId") Long organizationId);

}
