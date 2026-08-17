package com.thevip.guide.dto;

import com.thevip.guide.entity.Guide;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record GuideAdminResponse(
        Long id,
        String guideType,
        Long platformId,
        String title,
        List<String> imageUrls,
        boolean active,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    // imageUrls는 지연 로딩 @ElementCollection이라, 트랜잭션이 열려 있는 이 시점에 리스트로
    // 미리 소비해둬야 한다(응답 직렬화 시점엔 세션이 이미 닫혀 있어 LazyInitializationException).
    public static GuideAdminResponse from(Guide guide) {
        return new GuideAdminResponse(
                guide.getId(),
                guide.getGuideType().name(),
                guide.getPlatformId(),
                guide.getTitle(),
                guide.getImageUrls().stream().filter(Objects::nonNull).toList(),
                guide.isActive(),
                guide.getSortOrder(),
                guide.getCreatedAt(),
                guide.getUpdatedAt());
    }
}
