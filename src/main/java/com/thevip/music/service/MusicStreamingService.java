package com.thevip.music.service;

import com.thevip.music.dto.MusicStreamingResponse;
import com.thevip.music.dto.StreamingLinkResponse;
import com.thevip.music.dto.StreamingOsGroupResponse;
import com.thevip.music.dto.StreamingPlatformResponse;
import com.thevip.music.entity.MusicStreamingLink;
import com.thevip.music.entity.OperatingSystem;
import com.thevip.music.repository.MusicStreamingLinkRepository;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.repository.PlatformRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 원클릭 스트리밍: 플랫폼 -> 운영체제 -> 링크 목록 순으로 드릴다운하는 구조로 응답을 조립한다.
 */
@Service
@RequiredArgsConstructor
public class MusicStreamingService {

    private final MusicStreamingLinkRepository musicStreamingLinkRepository;
    private final PlatformRepository platformRepository;

    @Transactional(readOnly = true)
    public MusicStreamingResponse getStreamingPlatforms() {
        List<MusicStreamingLink> links = musicStreamingLinkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc();

        Map<Long, List<MusicStreamingLink>> linksByPlatform = links.stream()
                .collect(Collectors.groupingBy(MusicStreamingLink::getPlatformId, LinkedHashMap::new,
                        Collectors.toList()));

        List<StreamingPlatformResponse> platforms = linksByPlatform.entrySet().stream()
                .map(entry -> toPlatformResponse(entry.getKey(), entry.getValue()))
                .filter(platform -> platform != null)
                .toList();

        return new MusicStreamingResponse(platforms);
    }

    private StreamingPlatformResponse toPlatformResponse(Long platformId, List<MusicStreamingLink> links) {
        Platform platform = platformRepository.findById(platformId).orElse(null);
        if (platform == null) {
            return null;
        }

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
