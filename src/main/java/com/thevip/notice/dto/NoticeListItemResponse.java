package com.thevip.notice.dto;

import com.thevip.notice.entity.Notice;
import java.time.LocalDateTime;

public record NoticeListItemResponse(Long id, String title, LocalDateTime createdAt, boolean pinned) {

    public static NoticeListItemResponse from(Notice notice) {
        return new NoticeListItemResponse(notice.getId(), notice.getTitle(), notice.getCreatedAt(), notice.isPinned());
    }
}
