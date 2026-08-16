package com.thevip.vote.dto;

import java.util.List;

public record VoteTodayResponse(
        VoteUrgentResponse urgent,
        List<VoteSummaryResponse> dueSoonVotes,
        List<VoteSummaryResponse> votes) {
}
