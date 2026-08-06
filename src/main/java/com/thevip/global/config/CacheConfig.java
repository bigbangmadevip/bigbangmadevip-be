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
    public static final String HOME_URGENT = "homeUrgent";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                // 카탈로그는 TTL 없이 어드민 변경 시점에만 @CacheEvict로 갱신 (어드민 API는 추후 구현)
                new CaffeineCache(CHEERING_CATALOG, Caffeine.newBuilder().build()),
                // homeUrgent는 단순 boolean 플래그라 관리자가 토글할 때만 값이 바뀐다.
                // TTL 없이 어드민 토글 시점에 @CacheEvict로 갱신 (어드민 API는 추후 구현)
                new CaffeineCache(HOME_URGENT, Caffeine.newBuilder().build())));
        return manager;
    }
}
