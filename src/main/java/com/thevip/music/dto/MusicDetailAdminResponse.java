package com.thevip.music.dto;

import com.thevip.music.entity.MusicDetail;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record MusicDetailAdminResponse(
        Long id,
        String category,
        String title,
        String songName,
        List<Long> platformIds,
        String platformUrl,
        LocalDateTime eventAt,
        String description,
        List<String> checklist,
        List<String> imageUrls,
        List<Long> guideIds,
        Long cheeringItemId,
        boolean menuUrgent,
        String urgentContent,
        boolean todayExposed,
        boolean active,
        LocalDateTime scheduledAt,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    // @ElementCollection 필드는 지연 로딩이라, 트랜잭션이 열려 있는 이 시점에 리스트로
    // 미리 소비해둬야 한다(응답 직렬화 시점엔 세션이 이미 닫혀 있어 LazyInitializationException).
    public static MusicDetailAdminResponse from(MusicDetail detail) {
        return new MusicDetailAdminResponse(
                detail.getId(),
                detail.getCategory().name(),
                detail.getTitle(),
                detail.getSongName(),
                detail.getPlatformIds().stream().filter(Objects::nonNull).toList(),
                detail.getPlatformUrl(),
                detail.getEventAt(),
                detail.getDescription(),
                detail.getChecklist().stream().filter(Objects::nonNull).toList(),
                detail.getImageUrls().stream().filter(Objects::nonNull).toList(),
                detail.getGuideIds().stream().filter(Objects::nonNull).toList(),
                detail.getCheeringItemId(),
                detail.isMenuUrgent(),
                detail.getUrgentContent(),
                detail.isTodayExposed(),
                detail.isActive(),
                detail.getScheduledAt(),
                detail.getSortOrder(),
                detail.getCreatedAt(),
                detail.getUpdatedAt());
    }
}
