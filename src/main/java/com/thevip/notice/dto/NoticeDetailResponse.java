package com.thevip.notice.dto;

import com.thevip.notice.entity.Notice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record NoticeDetailResponse(
        Long id,
        String title,
        LocalDateTime createdAt,
        String content,
        List<String> imageUrls) {

    public static NoticeDetailResponse from(Notice notice) {
        return new NoticeDetailResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getCreatedAt(),
                notice.getContent(),
                notice.getImageUrls().stream().filter(Objects::nonNull).toList());
    }
}
