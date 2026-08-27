package com.thevip.platform.dto;

import com.thevip.platform.entity.PlatformRegion;
import com.thevip.platform.entity.PlatformType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlatformAdminRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull PlatformType type,
        @NotNull PlatformRegion region,
        String iconUrl,
        boolean active) {
}
