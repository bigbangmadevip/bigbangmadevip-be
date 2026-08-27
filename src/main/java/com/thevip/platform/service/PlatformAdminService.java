package com.thevip.platform.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.platform.dto.PlatformAdminRequest;
import com.thevip.platform.dto.PlatformAdminResponse;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.repository.PlatformRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public PlatformAdminResponse create(PlatformAdminRequest request) {
        validateCodeNotDuplicated(request.code(), null);
        Platform platform = Platform.of(request.name(), request.code(), request.type(), request.region(),
                request.iconUrl());
        platform.updateActive(request.active());
        platformRepository.save(platform);
        return PlatformAdminResponse.from(platform);
    }

    @Transactional
    public PlatformAdminResponse update(Long id, PlatformAdminRequest request) {
        Platform platform = getEntity(id);
        validateCodeNotDuplicated(request.code(), id);
        platform.update(request.name(), request.code(), request.type(), request.region(), request.iconUrl());
        platform.updateActive(request.active());
        return PlatformAdminResponse.from(platform);
    }

    // code는 어드민 API에서 platformId 대신 쓰는 안정적인 참조값이라 유니크해야 한다. DB 유니크
    // 제약에만 맡기면 예외 메시지가 불친절해서(DataIntegrityViolationException) 미리 확인한다.
    private void validateCodeNotDuplicated(String code, Long excludeId) {
        platformRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.PLATFORM_CODE_ALREADY_EXISTS);
                });
    }

    private Platform getEntity(Long id) {
        return platformRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 플랫폼입니다."));
    }
}
