package com.thevip.music.service;

import com.thevip.music.dto.MusicStreamingResponse;
import com.thevip.music.dto.MusicStreamingUrgentResponse;
import com.thevip.music.dto.StreamingLinkResponse;
import com.thevip.music.dto.StreamingOsGroupResponse;
import com.thevip.music.dto.StreamingPlatformResponse;
import com.thevip.music.entity.MusicStreamingImage;
import com.thevip.music.entity.MusicStreamingLink;
import com.thevip.music.entity.OperatingSystem;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.music.repository.MusicStreamingImageRepository;
import com.thevip.music.repository.MusicStreamingLinkRepository;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.repository.PlatformRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 음원 메뉴 스트리밍 탭 메인 화면: 긴급 총공 배너(음원만, menuUrgent) + 원클릭 스트리밍
 * (플랫폼 -> 운영체제 -> 링크 목록 드릴다운) + 스트리밍 리스트(추천 플레이리스트 이미지)를 조립한다.
 */
@Service
@RequiredArgsConstructor
public class MusicStreamingService {

    private final MusicDetailRepository musicDetailRepository;
    private final MusicStreamingLinkRepository musicStreamingLinkRepository;
    private final MusicStreamingImageRepository musicStreamingImageRepository;
    private final PlatformRepository platformRepository;

    @Transactional(readOnly = true)
    public MusicStreamingResponse getStreamingPlatforms() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        MusicStreamingUrgentResponse urgent = musicDetailRepository
                .findVisibleMenuUrgent(now, startOfToday, startOfTomorrow)
                .stream().findFirst()
                .map(MusicStreamingUrgentResponse::from)
                .orElse(null);

        List<MusicStreamingLink> links = musicStreamingLinkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc();

        Map<Long, List<MusicStreamingLink>> linksByPlatform = links.stream()
                .collect(Collectors.groupingBy(MusicStreamingLink::getPlatformId, LinkedHashMap::new,
                        Collectors.toList()));

        List<StreamingPlatformResponse> platforms = linksByPlatform.entrySet().stream()
                .map(entry -> toPlatformResponse(entry.getKey(), entry.getValue()))
                .filter(platform -> platform != null)
                .toList();

        List<MusicStreamingImage> images = musicStreamingImageRepository.findByActiveTrueOrderBySortOrderAsc();
        List<String> streamingImageUrls = images.stream().map(MusicStreamingImage::getImageUrl).toList();
        // 이미지 자체의 등록/수정 시각 중 가장 최근 값을 "최신 업데이트"로 노출한다 (별도 관리 필드 없이 데이터로 계산).
        LocalDateTime imagesUpdatedAt = images.stream()
                .map(image -> image.getUpdatedAt() != null ? image.getUpdatedAt() : image.getCreatedAt())
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new MusicStreamingResponse(urgent, platforms, streamingImageUrls, imagesUpdatedAt);
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
