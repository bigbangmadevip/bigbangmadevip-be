package com.thevip.guide.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.guide.dto.GuideAdminRequest;
import com.thevip.guide.dto.GuideAdminResponse;
import com.thevip.guide.service.GuideAdminService;
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
public class GuideAdminController {

    private final GuideAdminService guideAdminService;

    @GetMapping("/api/v1/admin/guides")
    public ApiResponse<List<GuideAdminResponse>> list() {
        return ApiResponse.success(guideAdminService.list());
    }

    @GetMapping("/api/v1/admin/guides/{guideId}")
    public ApiResponse<GuideAdminResponse> get(@PathVariable Long guideId) {
        return ApiResponse.success(guideAdminService.get(guideId));
    }

    @PostMapping("/api/v1/admin/guides")
    public ApiResponse<GuideAdminResponse> create(@Valid @RequestBody GuideAdminRequest request) {
        return ApiResponse.success(guideAdminService.create(request));
    }

    @PutMapping("/api/v1/admin/guides/{guideId}")
    public ApiResponse<GuideAdminResponse> update(@PathVariable Long guideId,
            @Valid @RequestBody GuideAdminRequest request) {
        return ApiResponse.success(guideAdminService.update(guideId, request));
    }
}
