package com.thevip.music.dto;

import com.thevip.music.entity.OperatingSystem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MusicStreamingLinkAdminRequest(
        Long platformId,
        @NotNull OperatingSystem os,
        @NotBlank String label,
        @NotBlank String url,
        boolean active,
        int sortOrder) {
}
