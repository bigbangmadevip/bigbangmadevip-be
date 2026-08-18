package com.thevip.music.dto;

import com.thevip.music.entity.MusicStreamingLink;

public record StreamingLinkResponse(String label, String url, boolean active) {

    public static StreamingLinkResponse from(MusicStreamingLink link) {
        return new StreamingLinkResponse(link.getLabel(), link.getUrl(), link.isActive());
    }
}
