package com.thevip.vote.dto;

import com.thevip.vote.entity.VoteCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record VoteDetailAdminRequest(
        @NotNull VoteCategory category,
        @NotBlank @Size(max = 100) String title,
        Long musicShowId,
        String rewardDescription,
        List<String> platformCodes,
        String platformUrl,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        List<String> checklist,
        List<String> imageUrls,
        List<Long> guideIds,
        @Size(max = 30) String ctaButtonLabel,
        boolean menuUrgent,
        @Size(max = 26) String urgentContent,
        boolean active,
        LocalDateTime scheduledAt,
        boolean pushEnabled,
        LocalDateTime pushSendAt,
        @Size(max = 26) String pushTitle,
        @Size(max = 26) String pushBody) {
}
