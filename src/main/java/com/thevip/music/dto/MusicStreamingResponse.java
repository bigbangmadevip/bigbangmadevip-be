package com.thevip.music.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MusicStreamingResponse(
        MusicStreamingUrgentResponse urgent,
        List<StreamingPlatformResponse> platforms,
        List<String> streamingImageUrls,
        LocalDateTime imagesUpdatedAt) {
}
