package com.thevip.vote.dto;

import com.thevip.guide.entity.Guide;

public record VoteDetailGuideResponse(Long guideId, String guideType, String title) {

    public static VoteDetailGuideResponse from(Guide guide) {
        return new VoteDetailGuideResponse(guide.getId(), guide.getGuideType().name(), guide.getTitle());
    }
}
