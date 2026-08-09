package com.thevip.music.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.guide.repository.GuideRepository;
import com.thevip.music.dto.MusicDetailGuideResponse;
import com.thevip.music.dto.MusicDetailResponse;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MusicDetailService {

    private final MusicDetailRepository musicDetailRepository;
    private final PlatformRepository platformRepository;
    private final GuideRepository guideRepository;

    @Transactional(readOnly = true)
    public MusicDetailResponse getDetail(Long id) {
        MusicDetail detail = musicDetailRepository.findById(id)
                .filter(MusicDetail::isActive)
                .filter(this::isVisible)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 음원 상세입니다."));

        List<String> platformNames = platformRepository.findNamesByIds(detail.getPlatformIds());
        List<MusicDetailGuideResponse> guides = guideRepository.findActiveByIds(detail.getGuideIds()).stream()
                .map(MusicDetailGuideResponse::from)
                .toList();

        return MusicDetailResponse.from(detail, platformNames, guides);
    }

    // scheduledAt이 지정돼 있고 아직 지나지 않았으면 예약 대기중이라 상세 접근도 막는다 (배치 없이 조회 시점 계산).
    private boolean isVisible(MusicDetail detail) {
        return detail.getScheduledAt() == null || !detail.getScheduledAt().isAfter(LocalDateTime.now());
    }
}
