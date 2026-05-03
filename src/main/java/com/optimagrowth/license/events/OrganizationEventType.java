package com.optimagrowth.license.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OrganizationEventType {
    @JsonProperty("organization.created")
    CREATED,

    @JsonProperty("organization.updated")
    UPDATED,

    @JsonProperty("organization.deleted")
    DELETED
}
