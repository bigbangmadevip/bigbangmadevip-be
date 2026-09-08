package com.thevip.vote.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.guide.repository.GuideRepository;
import com.thevip.vote.dto.VoteDetailGuideResponse;
import com.thevip.vote.dto.VoteDetailResponse;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteDetailService {

    private final VoteDetailRepository voteDetailRepository;
    private final VoteDetailPlatformResolver voteDetailPlatformResolver;
    private final GuideRepository guideRepository;

    @Transactional(readOnly = true)
    public VoteDetailResponse getDetail(Long id) {
        VoteDetail detail = voteDetailRepository.findById(id)
                .filter(VoteDetail::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 투표 상세입니다."));

        List<String> platformNames = voteDetailPlatformResolver.resolveNames(detail);
        List<VoteDetailGuideResponse> guides = guideRepository.findActiveByIds(detail.getGuideIds()).stream()
                .map(VoteDetailGuideResponse::from)
                .toList();

        return VoteDetailResponse.from(detail, platformNames, guides);
    }
}
