package com.thevip.vote.dto;

import com.thevip.vote.entity.MusicShow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record MusicShowAdminResponse(
        Long id,
        String name,
        List<Long> platformIds,
        boolean active,
        int sortOrder,
        String channel,
        String broadcastTime,
        String iconUrl,
        String description,
        List<Long> guideIds,
        LocalDateTime createdAt) {

    // @ElementCollection 필드는 지연 로딩이라, 트랜잭션이 열려 있는 이 시점에 리스트로
    // 미리 소비해둬야 한다(응답 직렬화 시점엔 세션이 이미 닫혀 있어 LazyInitializationException).
    public static MusicShowAdminResponse from(MusicShow show) {
        return new MusicShowAdminResponse(
                show.getId(),
                show.getName(),
                show.getPlatformIds().stream().filter(Objects::nonNull).toList(),
                show.isActive(),
                show.getSortOrder(),
                show.getChannel(),
                show.getBroadcastTime(),
                show.getIconUrl(),
                show.getDescription(),
                show.getGuideIds().stream().filter(Objects::nonNull).toList(),
                show.getCreatedAt());
    }
}
