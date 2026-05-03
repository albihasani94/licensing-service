package com.optimagrowth.license.events;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class OrganizationEventListener {

    @Bean
    Consumer<OrganizationChangeEvent> organizationEvents(OrganizationChangeHandler handler) {
        return handler::handle;
    }
}
