package com.optimagrowth.license.service;

import com.optimagrowth.license.client.OrganizationFeignClient;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.util.ClientType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(properties = {
        "spring.cache.type=redis",
        "licensing.cache.organizations.time-to-live=30s"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrganizationLookupRedisCacheTest {

    private static final Long ORGANIZATION_ID = 42L;
    private static final long CACHE_TTL_SECONDS = 30L;
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:8.6.2-alpine");
    private static final RedisCallback<Void> FLUSH_DATABASE = connection -> {
        connection.serverCommands().flushDb();
        return null;
    };

    @Container
    @ServiceConnection
    private static final GenericContainer<?> REDIS = new GenericContainer<>(REDIS_IMAGE)
            .withExposedPorts(6379);

    @MockitoBean
    private OrganizationFeignClient organizationFeignClient;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private OrganizationLookupService organizationLookupService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        redisTemplate.execute(FLUSH_DATABASE);
    }

    @Test
    void storesOrganizationInRedisAsJsonWithTtlAfterFirstCacheMiss() {
        when(organizationFeignClient.getOrganization(ORGANIZATION_ID))
                .thenReturn(organization());

        assertThat(lookupOrganization().getName()).isEqualTo("Original Organization");
        assertThat(lookupOrganization().getName()).isEqualTo("Original Organization");

        assertThat(cachedOrganizationJson())
                .contains("\"organizationId\":42")
                .contains("\"name\":\"Original Organization\"");

        assertThat(organizationCacheTtlSeconds()).isBetween(1L, CACHE_TTL_SECONDS);
        assertThat(organizationCacheStatistics().getMisses()).isEqualTo(1);
        assertThat(organizationCacheStatistics().getHits()).isEqualTo(1);
        verify(organizationFeignClient).getOrganization(ORGANIZATION_ID);
        verifyNoMoreInteractions(organizationFeignClient);
    }

    private Organization lookupOrganization() {
        return organizationLookupService.getOrganization(ORGANIZATION_ID, ClientType.FEIGN);
    }

    private String cachedOrganizationJson() {
        return redisTemplate.opsForValue().get(organizationCacheKey());
    }

    private Long organizationCacheTtlSeconds() {
        return redisTemplate.getExpire(organizationCacheKey());
    }

    private org.springframework.data.redis.cache.CacheStatistics organizationCacheStatistics() {
        return ((RedisCache) cacheManager.getCache(OrganizationLookupService.ORGANIZATIONS_CACHE)).getStatistics();
    }

    private static String organizationCacheKey() {
        return OrganizationLookupService.ORGANIZATIONS_CACHE + "::" + ORGANIZATION_ID;
    }

    private static Organization organization() {
        Organization organization = new Organization();
        organization.setOrganizationId(ORGANIZATION_ID);
        organization.setName("Original Organization");
        organization.setContactName("Jane Doe");
        organization.setContactEmail("jane@example.com");
        organization.setContactPhone("555-0100");
        return organization;
    }
}