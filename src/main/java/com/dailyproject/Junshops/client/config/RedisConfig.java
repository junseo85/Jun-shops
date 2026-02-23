package com.dailyproject.Junshops.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Configuration
 *
 * PURPOSE:
 * - Configure Redis connection
 * - Set up serialization (convert objects to/from JSON)
 * - Configure cache manager
 * - Set cache TTL (time-to-live)
 *
 * WHY NEEDED?
 * - Redis stores bytes, we need to convert Java objects
 * - Define how long data stays in cache
 * - Configure cache behavior
 */
@Configuration
//@EnableCaching  // ✅ Enable Spring's caching support
public class RedisConfig {

    /**
     * Configure ObjectMapper for Redis serialization
     *
     * WHY?
     * - Convert Java objects to JSON for storage in Redis
     * - Handle Java 8 date/time types (LocalDateTime)
     * - Configure JSON format
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Handle Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());

        // Don't write dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        //Enable type information in JSON
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class) // Allow all types
                .build();

        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        return mapper;
    }

    /**
     * Configure RedisTemplate for manual cache operations
     *
     * USAGE:
     * - Manual cache operations (optional)
     * - Direct Redis access if needed
     *
     * SERIALIZERS:
     * - Key: String (product:123)
     * - Value: JSON (entire object)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer: Convert keys to strings
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        // Value serializer: Convert objects to JSON
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure CacheManager for @Cacheable annotations
     *
     * PURPOSE:
     * - Manages all caches in application
     * - Sets default TTL (time-to-live)
     * - Configures serialization
     *
     * TTL EXPLAINED:
     * - How long data stays in cache before expiring
     * - After TTL, cache is invalidated (forces refresh)
     * - Balances freshness vs performance
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        // Serializers
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))  // ✅ Cache for 1 hour
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();  // Don't cache null results

        // Build cache manager
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .transactionAware()  // Support for @Transactional
                .build();
    }
}