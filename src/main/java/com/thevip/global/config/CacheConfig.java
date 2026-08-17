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
    public static final String HOME_TODAY_SCHEDULE = "homeTodaySchedule";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                // 카탈로그는 TTL 없이 어드민 변경 시점에만 @CacheEvict로 갱신 (CheeringItemAdminService 참고)
                new CaffeineCache(CHEERING_CATALOG, Caffeine.newBuilder().build()),
                // homeUrgent/homeTodaySchedule은 단순 boolean 플래그라 관리자가 토글할 때만 값이 바뀐다.
                // TTL 없이 어드민 변경 시점에 @CacheEvict로 갱신 (MusicDetail/VoteDetail/Platform
                // AdminService 참고 — 이 셋 중 하나라도 캐시된 값에 영향을 줄 수 있어 셋 다 비운다)
                new CaffeineCache(HOME_URGENT, Caffeine.newBuilder().build()),
                new CaffeineCache(HOME_TODAY_SCHEDULE, Caffeine.newBuilder().build())));
        return manager;
    }
}
