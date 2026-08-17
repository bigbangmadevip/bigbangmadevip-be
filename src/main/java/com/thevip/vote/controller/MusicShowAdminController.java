package com.thevip.vote.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.vote.dto.MusicShowAdminRequest;
import com.thevip.vote.dto.MusicShowAdminResponse;
import com.thevip.vote.dto.MusicShowVoteRoundAdminRequest;
import com.thevip.vote.dto.MusicShowVoteRoundAdminResponse;
import com.thevip.vote.service.MusicShowAdminService;
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
public class MusicShowAdminController {

    private final MusicShowAdminService musicShowAdminService;

    @GetMapping("/api/v1/admin/vote/shows")
    public ApiResponse<List<MusicShowAdminResponse>> list() {
        return ApiResponse.success(musicShowAdminService.list());
    }

    @GetMapping("/api/v1/admin/vote/shows/{showId}")
    public ApiResponse<MusicShowAdminResponse> get(@PathVariable Long showId) {
        return ApiResponse.success(musicShowAdminService.get(showId));
    }

    @PostMapping("/api/v1/admin/vote/shows")
    public ApiResponse<MusicShowAdminResponse> create(@Valid @RequestBody MusicShowAdminRequest request) {
        return ApiResponse.success(musicShowAdminService.create(request));
    }

    @PutMapping("/api/v1/admin/vote/shows/{showId}")
    public ApiResponse<MusicShowAdminResponse> update(@PathVariable Long showId,
            @Valid @RequestBody MusicShowAdminRequest request) {
        return ApiResponse.success(musicShowAdminService.update(showId, request));
    }

    @GetMapping("/api/v1/admin/vote/shows/{showId}/rounds")
    public ApiResponse<List<MusicShowVoteRoundAdminResponse>> listRounds(@PathVariable Long showId) {
        return ApiResponse.success(musicShowAdminService.listRounds(showId));
    }

    @PostMapping("/api/v1/admin/vote/shows/{showId}/rounds")
    public ApiResponse<MusicShowVoteRoundAdminResponse> createRound(@PathVariable Long showId,
            @Valid @RequestBody MusicShowVoteRoundAdminRequest request) {
        return ApiResponse.success(musicShowAdminService.createRound(showId, request));
    }

    @PutMapping("/api/v1/admin/vote/shows/{showId}/rounds/{roundId}")
    public ApiResponse<MusicShowVoteRoundAdminResponse> updateRound(@PathVariable Long showId,
            @PathVariable Long roundId, @Valid @RequestBody MusicShowVoteRoundAdminRequest request) {
        return ApiResponse.success(musicShowAdminService.updateRound(showId, roundId, request));
    }
}
