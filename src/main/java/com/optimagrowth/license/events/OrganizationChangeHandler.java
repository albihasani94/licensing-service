package com.optimagrowth.license.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrganizationChangeHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationChangeHandler.class);

    public void handle(OrganizationChangeEvent event) {
        logger.info("Received organization change event {} for organization id {}",
                event.eventType(), event.aggregateId());
    }
}
