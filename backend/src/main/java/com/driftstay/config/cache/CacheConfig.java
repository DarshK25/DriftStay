package com.driftstay.config.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration.
 *
 * Cache name conventions:
 * - properties:      Property listings (TTL: 1 hour)
 * - property:slug:   Individual property by slug (TTL: 1 hour)
 * - amenities:       All amenities (TTL: 24 hours)
 * - cities:          Available cities (TTL: 24 hours)
 * - roomTypes:       Room types (TTL: 24 hours)
 * - featuredProps:   Featured properties for homepage (TTL: 30 min)
 * - propertySearch:  Search results (TTL: 10 min)
 *
 * Never cache:
 * - Bookings (contains PII and changes frequently)
 * - Payments (financial data)
 * - Users (PII)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_PROPERTIES = "properties";
    public static final String CACHE_PROPERTY_SLUG = "propertyBySlug";
    public static final String CACHE_AMENITIES = "amenities";
    public static final String CACHE_CITIES = "cities";
    public static final String CACHE_ROOM_TYPES = "roomTypes";
    public static final String CACHE_FEATURED = "featuredProperties";
    public static final String CACHE_SEARCH = "propertySearch";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put(CACHE_PROPERTIES, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(CACHE_PROPERTY_SLUG, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(CACHE_AMENITIES, defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put(CACHE_CITIES, defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put(CACHE_ROOM_TYPES, defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put(CACHE_FEATURED, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(CACHE_SEARCH, defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
