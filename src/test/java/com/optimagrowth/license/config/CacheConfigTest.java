package com.optimagrowth.license.config;

import com.optimagrowth.license.model.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void organizationCacheUsesJsonSerialization() {
        RedisCacheConfiguration cacheConfiguration = CacheConfig.organizationCacheConfiguration(Duration.ofMinutes(10));

        Organization organization = new Organization();
        organization.setOrganizationId(42L);
        organization.setName("Optima Growth");
        organization.setContactName("Jane Doe");
        organization.setContactEmail("jane@example.com");
        organization.setContactPhone("555-0100");

        Object cachedValue = cacheConfiguration.getValueSerializationPair()
                .read(cacheConfiguration.getValueSerializationPair().write(organization));

        assertThat(cachedValue)
                .isInstanceOf(Organization.class)
                .extracting("organizationId", "name", "contactName", "contactEmail", "contactPhone")
                .containsExactly(42L, "Optima Growth", "Jane Doe", "jane@example.com", "555-0100");
        assertThat(cacheConfiguration.getAllowCacheNullValues()).isFalse();
    }

    @Test
    void organizationCacheUsesConfiguredTtl() {
        RedisCacheConfiguration cacheConfiguration = CacheConfig.organizationCacheConfiguration(Duration.ofMinutes(10));

        Duration ttl = cacheConfiguration.getTtlFunction()
                .getTimeToLive("organizations::42", RedisCacheWriter.TtlFunction.NO_EXPIRATION);

        assertThat(ttl).isEqualTo(Duration.ofMinutes(10));
    }
}
