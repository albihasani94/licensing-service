package com.optimagrowth.license.client;

import com.optimagrowth.license.model.Organization;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface OrganizationHttpInterface {

    @GetExchange("organization-service/v1/organization/{organizationId}")
    Organization getOrganization(@PathVariable("organizationId") Long organizationId);

}
