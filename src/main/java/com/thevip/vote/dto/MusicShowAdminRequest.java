package com.thevip.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MusicShowAdminRequest(
        @NotBlank @Size(max = 50) String name,
        List<Long> platformIds,
        boolean active,
        int sortOrder,
        @Size(max = 20) String channel,
        @Size(max = 50) String broadcastTime,
        String iconUrl,
        String description,
        List<Long> guideIds) {
}
