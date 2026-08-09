package com.thevip.music.dto;

import java.util.List;

public record StreamingPlatformResponse(
        Long platformId,
        String name,
        String iconUrl,
        String region,
        List<StreamingOsGroupResponse> osGroups) {
}
