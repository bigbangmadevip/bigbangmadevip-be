package com.thevip.platform.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.platform.dto.PlatformAdminRequest;
import com.thevip.platform.dto.PlatformAdminResponse;
import com.thevip.platform.service.PlatformAdminService;
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
public class PlatformAdminController {

    private final PlatformAdminService platformAdminService;

    @GetMapping("/api/v1/admin/platforms")
    public ApiResponse<List<PlatformAdminResponse>> list() {
        return ApiResponse.success(platformAdminService.list());
    }

    @GetMapping("/api/v1/admin/platforms/{platformId}")
    public ApiResponse<PlatformAdminResponse> get(@PathVariable Long platformId) {
        return ApiResponse.success(platformAdminService.get(platformId));
    }

    @PostMapping("/api/v1/admin/platforms")
    public ApiResponse<PlatformAdminResponse> create(@Valid @RequestBody PlatformAdminRequest request) {
        return ApiResponse.success(platformAdminService.create(request));
    }

    @PutMapping("/api/v1/admin/platforms/{platformId}")
    public ApiResponse<PlatformAdminResponse> update(@PathVariable Long platformId,
            @Valid @RequestBody PlatformAdminRequest request) {
        return ApiResponse.success(platformAdminService.update(platformId, request));
    }
}
