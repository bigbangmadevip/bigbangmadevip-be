package com.thevip.music.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.guide.repository.GuideRepository;
import com.thevip.music.dto.MusicDetailGuideResponse;
import com.thevip.music.dto.MusicDetailResponse;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 음원 상세입니다."));

        List<String> platformNames = platformRepository.findNamesByIds(detail.getPlatformIds());
        List<MusicDetailGuideResponse> guides = guideRepository.findActiveByIds(detail.getGuideIds()).stream()
                .map(MusicDetailGuideResponse::from)
                .toList();

        return MusicDetailResponse.from(detail, platformNames, guides);
    }
}
