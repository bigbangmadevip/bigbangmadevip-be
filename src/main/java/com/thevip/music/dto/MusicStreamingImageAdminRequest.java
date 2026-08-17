package com.thevip.music.dto;

import jakarta.validation.constraints.NotBlank;

public record MusicStreamingImageAdminRequest(
        @NotBlank String imageUrl,
        boolean active,
        int sortOrder) {
}
