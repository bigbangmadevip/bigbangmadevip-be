package com.thevip.vote.dto;

import com.thevip.vote.entity.VoteDetail;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record VoteDetailAdminResponse(
        Long id,
        String category,
        String title,
        Long musicShowId,
        String rewardDescription,
        List<Long> platformIds,
        List<String> platformUrl,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        List<String> checklist,
        List<String> imageUrls,
        List<Long> guideIds,
        String ctaButtonLabel,
        boolean menuUrgent,
        String urgentContent,
        boolean active,
        LocalDateTime scheduledAt,
        boolean pushEnabled,
        LocalDateTime pushSendAt,
        String pushTitle,
        String pushBody,
        LocalDateTime pushSentAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    // @ElementCollection 필드는 지연 로딩이라, 트랜잭션이 열려 있는 이 시점에 리스트로
    // 미리 소비해둬야 한다(응답 직렬화 시점엔 세션이 이미 닫혀 있어 LazyInitializationException).
    public static VoteDetailAdminResponse from(VoteDetail detail) {
        return new VoteDetailAdminResponse(
                detail.getId(),
                detail.getCategory().name(),
                detail.getTitle(),
                detail.getMusicShowId(),
                detail.getRewardDescription(),
                detail.getPlatformIds().stream().filter(Objects::nonNull).toList(),
                detail.getPlatformUrl().stream().filter(Objects::nonNull).toList(),
                detail.getEventStartAt(),
                detail.getEventEndAt(),
                detail.getChecklist().stream().filter(Objects::nonNull).toList(),
                detail.getImageUrls().stream().filter(Objects::nonNull).toList(),
                detail.getGuideIds().stream().filter(Objects::nonNull).toList(),
                detail.getCtaButtonLabel(),
                detail.isMenuUrgent(),
                detail.getUrgentContent(),
                detail.isActive(),
                detail.getScheduledAt(),
                detail.isPushEnabled(),
                detail.getPushSendAt(),
                detail.getPushTitle(),
                detail.getPushBody(),
                detail.getPushSentAt(),
                detail.getCreatedAt(),
                detail.getUpdatedAt());
    }
}
