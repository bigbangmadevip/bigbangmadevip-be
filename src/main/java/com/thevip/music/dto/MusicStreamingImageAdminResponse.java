package com.thevip.music.dto;

import com.thevip.music.entity.MusicStreamingImage;
import java.time.LocalDateTime;

public record MusicStreamingImageAdminResponse(
        Long id,
        String imageUrl,
        boolean active,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static MusicStreamingImageAdminResponse from(MusicStreamingImage image) {
        return new MusicStreamingImageAdminResponse(
                image.getId(),
                image.getImageUrl(),
                image.isActive(),
                image.getSortOrder(),
                image.getCreatedAt(),
                image.getUpdatedAt());
    }
}
