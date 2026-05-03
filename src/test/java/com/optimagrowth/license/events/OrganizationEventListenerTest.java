package com.optimagrowth.license.events;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.MimeTypeUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.function.definition=organizationEvents",
        "spring.cloud.stream.bindings.organizationEvents-in-0.destination=organization.events",
        "spring.cloud.stream.bindings.organizationEvents-in-0.group=licensing-service-test",
        "spring.cloud.stream.bindings.organizationEvents-in-0.consumer.auto-startup=true"
})
@Import(TestChannelBinderConfiguration.class)
class OrganizationEventListenerTest {

    @Autowired
    private InputDestination inputDestination;

    @MockitoBean
    private OrganizationChangeHandler organizationChangeHandler;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void consumesOrganizationChangeEventsPublishedByOrganizationService() {
        String event = """
                {
                  "eventId": "9c7463a0-e0ff-4d5b-a179-c8e0d33f74e5",
                  "eventType": "organization.updated",
                  "aggregateType": "organization",
                  "aggregateId": 42,
                  "occurredAt": "2026-05-03T12:00:00Z",
                  "payload": {
                    "organizationId": 42,
                    "name": "Optima Growth",
                    "contactName": "Jane Doe",
                    "contactEmail": "jane@example.com",
                    "contactPhone": "555-0100"
                  }
                }
                """;

        inputDestination.send(MessageBuilder
                .withPayload(event.getBytes(StandardCharsets.UTF_8))
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build(), "organization.events");

        ArgumentCaptor<OrganizationChangeEvent> eventCaptor = ArgumentCaptor.forClass(OrganizationChangeEvent.class);
        verify(organizationChangeHandler, timeout(2000)).handle(eventCaptor.capture());

        OrganizationChangeEvent consumedEvent = eventCaptor.getValue();
        assertThat(consumedEvent.eventType()).isEqualTo(OrganizationEventType.UPDATED);
        assertThat(consumedEvent.aggregateType()).isEqualTo("organization");
        assertThat(consumedEvent.aggregateId()).isEqualTo(42L);
        assertThat(consumedEvent.payload().get("name").asText()).isEqualTo("Optima Growth");
    }
}
