package com.optimagrowth.license.config;

import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.service.OrganizationLookupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

    @Bean
    RedisCacheManagerBuilderCustomizer organizationCacheCustomizer(
            @Value("${licensing.cache.organizations.time-to-live:10m}") Duration organizationCacheTtl) {
        return builder -> builder
                .enableStatistics()
                .withCacheConfiguration(
                        OrganizationLookupService.ORGANIZATIONS_CACHE,
                        organizationCacheConfiguration(organizationCacheTtl));
    }

    static RedisCacheConfiguration organizationCacheConfiguration(Duration organizationCacheTtl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(organizationCacheTtl)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(Organization.class)));
    }

}
