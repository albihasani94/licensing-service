package com.optimagrowth.license.events;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record OrganizationChangeEvent(
        UUID eventId,
        OrganizationEventType eventType,
        String aggregateType,
        Long aggregateId,
        Instant occurredAt,
        JsonNode payload
) {
}
