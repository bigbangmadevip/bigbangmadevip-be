package com.thevip.vote.dto;

import com.thevip.vote.entity.VoteDetail;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record VoteDetailResponse(
        Long id,
        String category,
        String title,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        String rewardDescription,
        List<String> platformNames,
        List<String> platformUrl,
        String ctaButtonLabel,
        List<String> checklist,
        List<String> imageUrls,
        List<VoteDetailGuideResponse> guides) {

    public static VoteDetailResponse from(VoteDetail detail, List<String> platformNames,
            List<VoteDetailGuideResponse> guides) {
        return new VoteDetailResponse(
                detail.getId(),
                detail.getCategory().name(),
                detail.getTitle(),
                detail.getEventStartAt(),
                detail.getEventEndAt(),
                detail.getRewardDescription(),
                platformNames,
                detail.getPlatformUrl().stream().filter(Objects::nonNull).toList(),
                detail.getCtaButtonLabel(),
                detail.getChecklist().stream().filter(Objects::nonNull).toList(),
                detail.getImageUrls().stream().filter(Objects::nonNull).toList(),
                guides);
    }
}
