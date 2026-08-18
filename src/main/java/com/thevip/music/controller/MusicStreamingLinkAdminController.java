package com.thevip.music.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.music.dto.MusicStreamingLinkAdminResponse;
import com.thevip.music.dto.MusicStreamingLinkBatchRequest;
import com.thevip.music.service.MusicStreamingLinkAdminService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MusicStreamingLinkAdminController {

    private final MusicStreamingLinkAdminService musicStreamingLinkAdminService;

    @GetMapping("/api/v1/admin/music/streaming-links")
    public ApiResponse<List<MusicStreamingLinkAdminResponse>> list() {
        return ApiResponse.success(musicStreamingLinkAdminService.list());
    }

    @GetMapping("/api/v1/admin/music/streaming-links/{linkId}")
    public ApiResponse<MusicStreamingLinkAdminResponse> get(@PathVariable Long linkId) {
        return ApiResponse.success(musicStreamingLinkAdminService.get(linkId));
    }

    @PutMapping("/api/v1/admin/music/streaming-links/platforms/{platformId}")
    public ApiResponse<List<MusicStreamingLinkAdminResponse>> replaceForPlatform(@PathVariable Long platformId,
            @Valid @RequestBody MusicStreamingLinkBatchRequest request) {
        return ApiResponse.success(musicStreamingLinkAdminService.replaceForPlatform(platformId, request));
    }
}
