package com.thevip.music.dto;

import com.thevip.music.entity.MusicCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record MusicDetailAdminRequest(
        @NotNull MusicCategory category,
        @NotBlank @Size(max = 100) String title,
        @Size(max = 100) String songName,
        List<Long> platformIds,
        String platformUrl,
        LocalDateTime eventAt,
        String description,
        List<String> checklist,
        List<String> imageUrls,
        List<Long> guideIds,
        Long cheeringItemId,
        boolean menuUrgent,
        @Size(max = 26) String urgentContent,
        boolean todayExposed,
        boolean active,
        LocalDateTime scheduledAt,
        int sortOrder) {
}
