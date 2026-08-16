package com.thevip.vote.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.guide.repository.GuideRepository;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.dto.VoteDetailGuideResponse;
import com.thevip.vote.dto.VoteDetailResponse;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteDetailService {

    private final VoteDetailRepository voteDetailRepository;
    private final PlatformRepository platformRepository;
    private final GuideRepository guideRepository;

    @Transactional(readOnly = true)
    public VoteDetailResponse getDetail(Long id) {
        VoteDetail detail = voteDetailRepository.findById(id)
                .filter(VoteDetail::isActive)
                .filter(this::isVisible)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 투표 상세입니다."));

        List<String> platformNames = platformRepository.findNamesByIds(detail.getPlatformIds());
        List<VoteDetailGuideResponse> guides = guideRepository.findActiveByIds(detail.getGuideIds()).stream()
                .map(VoteDetailGuideResponse::from)
                .toList();

        return VoteDetailResponse.from(detail, platformNames, guides);
    }

    // scheduledAt이 지정돼 있고 아직 지나지 않았으면 예약 대기중이라 상세 접근도 막는다 (배치 없이 조회 시점 계산).
    private boolean isVisible(VoteDetail detail) {
        return detail.getScheduledAt() == null || !detail.getScheduledAt().isAfter(LocalDateTime.now());
    }
}
