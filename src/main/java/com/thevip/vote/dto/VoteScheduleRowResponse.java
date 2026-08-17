package com.thevip.vote.dto;

import com.thevip.vote.entity.VoteRoundRow;

public record VoteScheduleRowResponse(String label, String value) {

    public static VoteScheduleRowResponse from(VoteRoundRow row) {
        return new VoteScheduleRowResponse(row.getLabel(), row.getValue());
    }
}
