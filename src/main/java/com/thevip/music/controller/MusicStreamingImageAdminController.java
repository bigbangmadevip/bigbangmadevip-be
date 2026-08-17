package com.thevip.music.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.music.dto.MusicStreamingImageAdminRequest;
import com.thevip.music.dto.MusicStreamingImageAdminResponse;
import com.thevip.music.service.MusicStreamingImageAdminService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MusicStreamingImageAdminController {

    private final MusicStreamingImageAdminService musicStreamingImageAdminService;

    @GetMapping("/api/v1/admin/music/streaming-images")
    public ApiResponse<List<MusicStreamingImageAdminResponse>> list() {
        return ApiResponse.success(musicStreamingImageAdminService.list());
    }

    @GetMapping("/api/v1/admin/music/streaming-images/{imageId}")
    public ApiResponse<MusicStreamingImageAdminResponse> get(@PathVariable Long imageId) {
        return ApiResponse.success(musicStreamingImageAdminService.get(imageId));
    }

    @PostMapping("/api/v1/admin/music/streaming-images")
    public ApiResponse<MusicStreamingImageAdminResponse> create(
            @Valid @RequestBody MusicStreamingImageAdminRequest request) {
        return ApiResponse.success(musicStreamingImageAdminService.create(request));
    }

    @PutMapping("/api/v1/admin/music/streaming-images/{imageId}")
    public ApiResponse<MusicStreamingImageAdminResponse> update(@PathVariable Long imageId,
            @Valid @RequestBody MusicStreamingImageAdminRequest request) {
        return ApiResponse.success(musicStreamingImageAdminService.update(imageId, request));
    }
}
