package com.thevip.music.dto;

import com.thevip.music.entity.MusicStreamingLink;
import java.time.LocalDateTime;

public record MusicStreamingLinkAdminResponse(
        Long id,
        Long platformId,
        String os,
        String label,
        String url,
        boolean active,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static MusicStreamingLinkAdminResponse from(MusicStreamingLink link) {
        return new MusicStreamingLinkAdminResponse(
                link.getId(),
                link.getPlatformId(),
                link.getOs().name(),
                link.getLabel(),
                link.getUrl(),
                link.isActive(),
                link.getSortOrder(),
                link.getCreatedAt(),
                link.getUpdatedAt());
    }
}
