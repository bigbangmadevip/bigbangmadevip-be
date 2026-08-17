package com.thevip.music.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.music.dto.MusicDetailAdminRequest;
import com.thevip.music.dto.MusicDetailAdminResponse;
import com.thevip.music.service.MusicDetailAdminService;
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
public class MusicDetailAdminController {

    private final MusicDetailAdminService musicDetailAdminService;

    @GetMapping("/api/v1/admin/music/details")
    public ApiResponse<List<MusicDetailAdminResponse>> list() {
        return ApiResponse.success(musicDetailAdminService.list());
    }

    @GetMapping("/api/v1/admin/music/details/{detailId}")
    public ApiResponse<MusicDetailAdminResponse> get(@PathVariable Long detailId) {
        return ApiResponse.success(musicDetailAdminService.get(detailId));
    }

    @PostMapping("/api/v1/admin/music/details")
    public ApiResponse<MusicDetailAdminResponse> create(@Valid @RequestBody MusicDetailAdminRequest request) {
        return ApiResponse.success(musicDetailAdminService.create(request));
    }

    @PutMapping("/api/v1/admin/music/details/{detailId}")
    public ApiResponse<MusicDetailAdminResponse> update(@PathVariable Long detailId,
            @Valid @RequestBody MusicDetailAdminRequest request) {
        return ApiResponse.success(musicDetailAdminService.update(detailId, request));
    }
}
