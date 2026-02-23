package com.dailyproject.Junshops.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cache management endpoint (TEMPORARY - for debugging)
 * DELETE THIS in production!
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Clear all caches through Spring Cache Manager
     */
    @DeleteMapping("/clear")
    public String clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                System.out.println("✅ Cleared cache: " + cacheName);
            }
        });
        return "✅ All caches cleared via CacheManager!";
    }

    /**
     * Nuclear option: Clear entire Redis database
     */
    @DeleteMapping("/flush")
    public String flushRedis() {
        try {
            Objects.requireNonNull(redisTemplate.getConnectionFactory())
                    .getConnection()
                    .flushDb();
            return "✅ Redis database flushed completely!";
        } catch (Exception e) {
            return "❌ Error flushing Redis: " + e.getMessage();
        }
    }

    /**
     * View all cache keys (for debugging)
     *
     * ✅ FIXED: Convert byte[] to String
     */
    @GetMapping("/keys")
    public Set<String> viewAllKeys() {
        Set<byte[]> keyBytes = Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .keys("*".getBytes());

        if (keyBytes == null) {
            return Set.of();  // Return empty set if null
        }

        // Convert byte[] to String
        return keyBytes.stream()
                .map(String::new)  // Convert each byte[] to String
                .collect(Collectors.toSet());
    }

    /**
     * Health check for Redis connection
     */
    @GetMapping("/ping")
    public String pingRedis() {
        try {
            String pong = Objects.requireNonNull(redisTemplate.getConnectionFactory())
                    .getConnection()
                    .ping();
            return "✅ Redis is connected! Response: " + pong;
        } catch (Exception e) {
            return "❌ Redis connection failed: " + e.getMessage();
        }
    }
}