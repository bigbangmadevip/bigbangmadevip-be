package com.thevip.vote.dto;

import com.thevip.vote.entity.MusicShowVoteRound;
import java.util.List;

public record VoteScheduleRoundResponse(Long id, String label, String time, String tone,
        List<VoteScheduleRowResponse> rows) {

    public static VoteScheduleRoundResponse from(MusicShowVoteRound round) {
        List<VoteScheduleRowResponse> rows = round.getRows().stream()
                .map(VoteScheduleRowResponse::from)
                .toList();
        return new VoteScheduleRoundResponse(round.getId(), round.getLabel(), round.getTime(), round.getTone(), rows);
    }
}
