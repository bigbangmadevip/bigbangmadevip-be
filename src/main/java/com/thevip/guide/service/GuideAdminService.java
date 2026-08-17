package com.thevip.guide.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.guide.dto.GuideAdminRequest;
import com.thevip.guide.dto.GuideAdminResponse;
import com.thevip.guide.entity.Guide;
import com.thevip.guide.repository.GuideRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuideAdminService {

    private final GuideRepository guideRepository;

    @Transactional(readOnly = true)
    public List<GuideAdminResponse> list() {
        return guideRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Guide::getSortOrder))
                .map(GuideAdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GuideAdminResponse get(Long id) {
        return GuideAdminResponse.from(getEntity(id));
    }

    @Transactional
    public GuideAdminResponse create(GuideAdminRequest request) {
        Guide guide = Guide.of(request.guideType(), request.platformId(), request.title(), request.sortOrder());
        guide.replaceImageUrls(nullSafe(request.imageUrls()));
        guide.updateActive(request.active());
        guideRepository.save(guide);
        return GuideAdminResponse.from(guide);
    }

    @Transactional
    public GuideAdminResponse update(Long id, GuideAdminRequest request) {
        Guide guide = getEntity(id);
        guide.update(request.guideType(), request.platformId(), request.title(), request.sortOrder());
        guide.replaceImageUrls(nullSafe(request.imageUrls()));
        guide.updateActive(request.active());
        return GuideAdminResponse.from(guide);
    }

    private Guide getEntity(Long id) {
        return guideRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 가이드입니다."));
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
