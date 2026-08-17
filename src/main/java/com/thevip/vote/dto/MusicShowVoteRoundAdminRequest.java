package com.thevip.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MusicShowVoteRoundAdminRequest(
        @NotBlank @Size(max = 50) String label,
        @Size(max = 50) String time,
        @Size(max = 20) String tone,
        boolean active,
        int sortOrder,
        List<VoteRoundRowRequest> rows) {
}
