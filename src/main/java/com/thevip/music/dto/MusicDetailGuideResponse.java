package com.thevip.music.dto;

import com.thevip.guide.entity.Guide;

public record MusicDetailGuideResponse(Long guideId, String guideType, String title) {

    public static MusicDetailGuideResponse from(Guide guide) {
        return new MusicDetailGuideResponse(guide.getId(), guide.getGuideType().name(), guide.getTitle());
    }
}
