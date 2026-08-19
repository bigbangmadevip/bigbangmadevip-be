package com.thevip.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CHEERING_CATALOG = "cheeringCatalog";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                // 카탈로그는 TTL 없이 어드민 변경 시점에만 @CacheEvict로 갱신 (CheeringItemAdminService 참고)
                new CaffeineCache(CHEERING_CATALOG, Caffeine.newBuilder().build())));
        return manager;
    }
}
