package com.thevip.platform.dto;

import com.thevip.platform.entity.Platform;

public record PlatformAdminResponse(
        Long id,
        String name,
        String code,
        String type,
        String region,
        String iconUrl,
        boolean active) {

    public static PlatformAdminResponse from(Platform platform) {
        return new PlatformAdminResponse(
                platform.getId(),
                platform.getName(),
                platform.getCode(),
                platform.getType().name(),
                platform.getRegion().name(),
                platform.getIconUrl(),
                platform.isActive());
    }
}
