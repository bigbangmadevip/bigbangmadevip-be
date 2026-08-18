package com.thevip.music.dto;

import com.thevip.music.entity.MusicStreamingLink;
import com.thevip.music.entity.OperatingSystem;
import com.thevip.platform.entity.Platform;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record StreamingPlatformResponse(
        Long platformId,
        String name,
        String iconUrl,
        String region,
        List<StreamingOsGroupResponse> osGroups) {

    public static StreamingPlatformResponse from(Platform platform, List<MusicStreamingLink> links) {
        Map<OperatingSystem, List<MusicStreamingLink>> linksByOs = links.stream()
                .collect(Collectors.groupingBy(MusicStreamingLink::getOs, LinkedHashMap::new, Collectors.toList()));

        List<StreamingOsGroupResponse> osGroups = linksByOs.entrySet().stream()
                .map(entry -> new StreamingOsGroupResponse(entry.getKey().name(),
                        entry.getValue().stream().map(StreamingLinkResponse::from).toList()))
                .toList();

        return new StreamingPlatformResponse(platform.getId(), platform.getName(), platform.getIconUrl(),
                platform.getRegion().name(), osGroups);
    }
}
