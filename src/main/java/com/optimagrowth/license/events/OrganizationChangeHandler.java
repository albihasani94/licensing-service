package com.optimagrowth.license.events;

import com.optimagrowth.license.service.OrganizationLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
public class OrganizationChangeHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationChangeHandler.class);

    @CacheEvict(
            cacheNames = OrganizationLookupService.ORGANIZATIONS_CACHE,
            key = "#event.aggregateId",
            condition = "#event.eventType().invalidatesCache()")
    public void handle(OrganizationChangeEvent event) {
        logger.info("Received organization change event {} for organization id {}", event.eventType(), event.aggregateId());
    }
}
