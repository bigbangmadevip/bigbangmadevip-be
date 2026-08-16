package com.thevip.vote.dto;

import com.thevip.vote.entity.VoteDetail;

public record VoteUrgentResponse(Long detailId, String urgentContent) {

    public static VoteUrgentResponse from(VoteDetail detail) {
        return new VoteUrgentResponse(detail.getId(), detail.getUrgentContent());
    }
}
