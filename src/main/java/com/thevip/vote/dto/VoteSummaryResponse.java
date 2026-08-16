package com.thevip.vote.dto;

import com.thevip.vote.entity.VoteDetail;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record VoteSummaryResponse(
        Long detailId,
        String category,
        String title,
        List<String> platformNames,
        LocalDateTime eventEndAt,
        String imageUrl) {

    public static VoteSummaryResponse from(VoteDetail detail, List<String> platformNames) {
        String imageUrl = detail.getImageUrls().stream().filter(Objects::nonNull).findFirst().orElse(null);
        return new VoteSummaryResponse(detail.getId(), detail.getCategory().name(), detail.getTitle(),
                platformNames, detail.getEventEndAt(), imageUrl);
    }
}
