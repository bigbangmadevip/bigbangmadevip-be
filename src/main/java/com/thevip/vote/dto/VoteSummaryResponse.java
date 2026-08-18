package com.thevip.vote.dto;

import com.thevip.vote.entity.VoteDetail;
import java.time.LocalDateTime;
import java.util.List;

public record VoteSummaryResponse(
        Long detailId,
        String category,
        String title,
        List<String> platformNames,
        LocalDateTime eventEndAt) {

    public static VoteSummaryResponse from(VoteDetail detail, List<String> platformNames) {
        return new VoteSummaryResponse(detail.getId(), detail.getCategory().name(), detail.getTitle(),
                platformNames, detail.getEventEndAt());
    }
}
