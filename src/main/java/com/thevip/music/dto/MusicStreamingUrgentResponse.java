package com.thevip.music.dto;

import com.thevip.music.entity.MusicDetail;

public record MusicStreamingUrgentResponse(Long detailId, String urgentContent) {

    public static MusicStreamingUrgentResponse from(MusicDetail detail) {
        return new MusicStreamingUrgentResponse(detail.getId(), detail.getUrgentContent());
    }
}
