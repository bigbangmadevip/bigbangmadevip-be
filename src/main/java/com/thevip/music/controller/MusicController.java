package com.thevip.music.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.music.dto.MusicStreamingResponse;
import com.thevip.music.service.MusicStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MusicController {

    private final MusicStreamingService musicStreamingService;

    @GetMapping("/api/v1/music/streaming")
    public ApiResponse<MusicStreamingResponse> streaming() {
        return ApiResponse.success(musicStreamingService.getStreamingPlatforms());
    }
}
