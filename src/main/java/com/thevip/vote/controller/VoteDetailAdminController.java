package com.thevip.vote.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.vote.dto.VoteDetailAdminRequest;
import com.thevip.vote.dto.VoteDetailAdminResponse;
import com.thevip.vote.service.VoteDetailAdminService;
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
public class VoteDetailAdminController {

    private final VoteDetailAdminService voteDetailAdminService;

    @GetMapping("/api/v1/admin/vote/details")
    public ApiResponse<List<VoteDetailAdminResponse>> list() {
        return ApiResponse.success(voteDetailAdminService.list());
    }

    @GetMapping("/api/v1/admin/vote/details/{detailId}")
    public ApiResponse<VoteDetailAdminResponse> get(@PathVariable Long detailId) {
        return ApiResponse.success(voteDetailAdminService.get(detailId));
    }

    @PostMapping("/api/v1/admin/vote/details")
    public ApiResponse<VoteDetailAdminResponse> create(@Valid @RequestBody VoteDetailAdminRequest request) {
        return ApiResponse.success(voteDetailAdminService.create(request));
    }

    @PutMapping("/api/v1/admin/vote/details/{detailId}")
    public ApiResponse<VoteDetailAdminResponse> update(@PathVariable Long detailId,
            @Valid @RequestBody VoteDetailAdminRequest request) {
        return ApiResponse.success(voteDetailAdminService.update(detailId, request));
    }
}
