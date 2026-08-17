package com.thevip.platform.service;

import com.thevip.global.config.CacheConfig;
import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.platform.dto.PlatformAdminRequest;
import com.thevip.platform.dto.PlatformAdminResponse;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.repository.PlatformRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlatformAdminService {

    private final PlatformRepository platformRepository;

    @Transactional(readOnly = true)
    public List<PlatformAdminResponse> list() {
        return platformRepository.findAll().stream()
                .map(PlatformAdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlatformAdminResponse get(Long id) {
        return PlatformAdminResponse.from(getEntity(id));
    }

    // 홈 캐시(homeUrgent/homeTodaySchedule)의 platformNames가 이 데이터를 참조하므로, 플랫폼을
    // 바꾸는 경로에서도 같이 비워줘야 한다 (안 그러면 재기동 전까지 옛 값이 계속 나간다).
    @CacheEvict(cacheNames = {CacheConfig.HOME_URGENT, CacheConfig.HOME_TODAY_SCHEDULE}, allEntries = true)
    @Transactional
    public PlatformAdminResponse create(PlatformAdminRequest request) {
        Platform platform = Platform.of(request.name(), request.type(), request.region(), request.iconUrl());
        platform.updateActive(request.active());
        platformRepository.save(platform);
        return PlatformAdminResponse.from(platform);
    }

    @CacheEvict(cacheNames = {CacheConfig.HOME_URGENT, CacheConfig.HOME_TODAY_SCHEDULE}, allEntries = true)
    @Transactional
    public PlatformAdminResponse update(Long id, PlatformAdminRequest request) {
        Platform platform = getEntity(id);
        platform.update(request.name(), request.type(), request.region(), request.iconUrl());
        platform.updateActive(request.active());
        return PlatformAdminResponse.from(platform);
    }

    private Platform getEntity(Long id) {
        return platformRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 플랫폼입니다."));
    }
}
