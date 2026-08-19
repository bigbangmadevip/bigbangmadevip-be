package com.thevip.notice.dto;

import com.thevip.notice.entity.Notice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record NoticeAdminResponse(
        Long id,
        String menuType,
        String title,
        String content,
        List<String> imageUrls,
        List<NoticeLinkResponse> links,
        boolean pinned,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String updatedBy) {

    // imageUrls/links는 지연 로딩 @ElementCollection이라, 트랜잭션이 열려 있는 이 시점에 리스트로
    // 미리 소비해둬야 한다(응답 직렬화 시점엔 세션이 이미 닫혀 있어 LazyInitializationException).
    public static NoticeAdminResponse from(Notice notice) {
        return new NoticeAdminResponse(
                notice.getId(),
                notice.getMenuType().name(),
                notice.getTitle(),
                notice.getContent(),
                notice.getImageUrls().stream().filter(Objects::nonNull).toList(),
                notice.getLinks().stream().filter(Objects::nonNull).map(NoticeLinkResponse::from).toList(),
                notice.isPinned(),
                notice.isActive(),
                notice.getCreatedAt(),
                notice.getUpdatedAt(),
                notice.getUpdatedBy());
    }
}
