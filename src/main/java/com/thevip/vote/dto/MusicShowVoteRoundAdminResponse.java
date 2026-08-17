package com.thevip.vote.dto;

import com.thevip.vote.entity.MusicShowVoteRound;
import java.time.LocalDateTime;
import java.util.List;

public record MusicShowVoteRoundAdminResponse(
        Long id,
        Long musicShowId,
        String label,
        String time,
        String tone,
        boolean active,
        int sortOrder,
        List<VoteScheduleRowResponse> rows,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static MusicShowVoteRoundAdminResponse from(MusicShowVoteRound round) {
        List<VoteScheduleRowResponse> rows = round.getRows().stream()
                .map(VoteScheduleRowResponse::from)
                .toList();
        return new MusicShowVoteRoundAdminResponse(
                round.getId(),
                round.getMusicShowId(),
                round.getLabel(),
                round.getTime(),
                round.getTone(),
                round.isActive(),
                round.getSortOrder(),
                rows,
                round.getCreatedAt(),
                round.getUpdatedAt());
    }
}
