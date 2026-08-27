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
        List<String> platformCodes,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        List<String> checklist,
        List<String> imageUrls,
        List<Long> guideIds,
        boolean menuUrgent,
        String urgentContent,
        boolean active,
        LocalDateTime scheduledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    // @ElementCollection 필드는 지연 로딩이라, 트랜잭션이 열려 있는 이 시점에 리스트로
    // 미리 소비해둬야 한다(응답 직렬화 시점엔 세션이 이미 닫혀 있어 LazyInitializationException).
    // platformCodes는 서비스에서 PlatformRepository로 미리 변환해서 넘겨준다.
    public static MusicDetailAdminResponse from(MusicDetail detail, List<String> platformCodes) {
        return new MusicDetailAdminResponse(
                detail.getId(),
                detail.getCategory().name(),
                detail.getTitle(),
                detail.getSongName(),
                platformCodes,
                detail.getEventStartAt(),
                detail.getEventEndAt(),
                detail.getChecklist().stream().filter(Objects::nonNull).toList(),
                detail.getImageUrls().stream().filter(Objects::nonNull).toList(),
                detail.getGuideIds().stream().filter(Objects::nonNull).toList(),
                detail.isMenuUrgent(),
                detail.getUrgentContent(),
                detail.isActive(),
                detail.getScheduledAt(),
                detail.getCreatedAt(),
                detail.getUpdatedAt());
    }
}
