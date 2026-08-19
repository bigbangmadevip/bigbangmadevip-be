package com.thevip.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record NoticeAdminRequest(
        @NotBlank @Size(max = 100) String title,
        String content,
        List<String> imageUrls,
        List<NoticeLinkRequest> links,
        boolean pinned,
        boolean active) {
}
