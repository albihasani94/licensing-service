package com.optimagrowth.license.service;

import com.optimagrowth.license.client.OrganizationClient;
import com.optimagrowth.license.client.OrganizationFeignClient;
import com.optimagrowth.license.config.ClientConfig;
import com.optimagrowth.license.events.OrganizationChangeEvent;
import com.optimagrowth.license.events.OrganizationChangeHandler;
import com.optimagrowth.license.events.OrganizationEventType;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.util.ClientType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = {
        OrganizationLookupService.class,
        OrganizationChangeHandler.class,
        OrganizationLookupCacheTest.InMemoryCacheTestConfig.class
})
class OrganizationLookupCacheTest {

    private static final Long ORGANIZATION_ID = 42L;

    @MockitoBean
    private DiscoveryClient discoveryClient;

    @MockitoBean
    private OrganizationFeignClient organizationFeignClient;

    @MockitoBean
    private OrganizationClient organizationClient;

    @Autowired
    private OrganizationLookupService organizationLookupService;

    @Autowired
    private OrganizationChangeHandler organizationChangeHandler;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        Objects.requireNonNull(cacheManager.getCache(OrganizationLookupService.ORGANIZATIONS_CACHE)).clear();
    }

    @Test
    void cachesOrganizationAfterFirstMiss() {
        Organization organization = organization();
        when(organizationFeignClient.getOrganization(ORGANIZATION_ID)).thenReturn(organization);

        Organization firstLookup = organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.FEIGN);
        Organization secondLookup = organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.FEIGN);

        assertThat(firstLookup.getName()).isEqualTo("Optima Growth");
        assertThat(secondLookup.getName()).isEqualTo("Optima Growth");
        verify(organizationFeignClient).getOrganization(ORGANIZATION_ID);
        verifyNoMoreInteractions(organizationFeignClient);
    }

    @Test
    void cachesOrganizationsByOrganizationIdAcrossClientTypes() {
        Organization organization = organization();
        when(organizationFeignClient.getOrganization(ORGANIZATION_ID)).thenReturn(organization);

        assertThat(organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.FEIGN).getName())
                .isEqualTo("Optima Growth");
        assertThat(organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.SPRING).getName())
                .isEqualTo("Optima Growth");

        verify(organizationFeignClient).getOrganization(ORGANIZATION_ID);
        verifyNoMoreInteractions(organizationFeignClient, organizationClient);
    }

    @Test
    void doesNotCacheNullOrganizationResponses() {
        when(organizationFeignClient.getOrganization(ORGANIZATION_ID)).thenReturn(null);

        assertThat(organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.FEIGN)).isNull();
        assertThat(organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.FEIGN)).isNull();

        verify(organizationFeignClient, times(2)).getOrganization(ORGANIZATION_ID);
        verifyNoMoreInteractions(organizationFeignClient);
    }

    @ParameterizedTest
    @EnumSource(value = OrganizationEventType.class, names = {"UPDATED", "DELETED"})
    void evictsCachedOrganizationsWhenOrganizationIsUpdatedOrDeleted(OrganizationEventType eventType) {
        Organization firstOrganization = organization("Original Organization");
        Organization refreshedOrganization = organization("Refreshed Organization");
        when(organizationFeignClient.getOrganization(ORGANIZATION_ID))
                .thenReturn(firstOrganization)
                .thenReturn(refreshedOrganization);

        assertThat(organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.FEIGN).getName())
                .isEqualTo("Original Organization");

        organizationChangeHandler.handle(event(eventType, ORGANIZATION_ID));

        assertThat(organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.FEIGN).getName())
                .isEqualTo("Refreshed Organization");
        verify(organizationFeignClient, times(2)).getOrganization(ORGANIZATION_ID);
        verifyNoMoreInteractions(organizationFeignClient);
    }

    @Test
    void doesNotEvictCachedOrganizationWhenOrganizationIsCreated() {
        Organization firstOrganization = organization("Original Organization");
        Organization refreshedOrganization = organization("Refreshed Organization");
        when(organizationFeignClient.getOrganization(ORGANIZATION_ID))
                .thenReturn(firstOrganization)
                .thenReturn(refreshedOrganization);

        assertThat(organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.FEIGN).getName())
                .isEqualTo("Original Organization");

        organizationChangeHandler.handle(event(OrganizationEventType.CREATED, ORGANIZATION_ID));

        assertThat(organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.FEIGN).getName())
                .isEqualTo("Original Organization");
        verify(organizationFeignClient).getOrganization(ORGANIZATION_ID);
        verifyNoMoreInteractions(organizationFeignClient);
    }

    private static Organization organization() {
        return organization("Optima Growth");
    }

    private static Organization organization(String name) {
        Organization organization = new Organization();
        organization.setOrganizationId(OrganizationLookupCacheTest.ORGANIZATION_ID);
        organization.setName(name);
        organization.setContactName("Jane Doe");
        organization.setContactEmail("jane@example.com");
        organization.setContactPhone("555-0100");
        return organization;
    }

    private static OrganizationChangeEvent event(OrganizationEventType eventType, Long organizationId) {
        return new OrganizationChangeEvent(
                UUID.randomUUID(),
                eventType,
                "organization",
                organizationId,
                Instant.now(),
                null
        );
    }

    @EnableCaching
    static class InMemoryCacheTestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(OrganizationLookupService.ORGANIZATIONS_CACHE);
        }

        @Bean
        @Qualifier(ClientConfig.ORGANIZATION_REST_CLIENT_BUILDER)
        RestClient.Builder organizationRestClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        @Qualifier("discoveryRestClient")
        RestClient discoveryRestClient() {
            return RestClient.create();
        }
    }
}
