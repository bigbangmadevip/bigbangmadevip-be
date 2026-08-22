package com.thevip.music.dto;

import com.thevip.music.entity.MusicDetail;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record MusicDetailResponse(
        Long id,
        String category,
        String title,
        String songName,
        List<String> platformNames,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        List<String> checklist,
        List<String> imageUrls,
        List<MusicDetailGuideResponse> guides) {

    public static MusicDetailResponse from(MusicDetail detail, List<String> platformNames,
            List<MusicDetailGuideResponse> guides) {
        return new MusicDetailResponse(
                detail.getId(),
                detail.getCategory().name(),
                detail.getTitle(),
                detail.getSongName(),
                platformNames,
                detail.getEventStartAt(),
                detail.getEventEndAt(),
                detail.getChecklist().stream().filter(Objects::nonNull).toList(),
                detail.getImageUrls().stream().filter(Objects::nonNull).toList(),
                guides);
    }
}
