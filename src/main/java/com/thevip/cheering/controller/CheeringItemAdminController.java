package com.thevip.cheering.controller;

import com.thevip.cheering.dto.CheeringItemAdminRequest;
import com.thevip.cheering.dto.CheeringItemAdminResponse;
import com.thevip.cheering.service.CheeringItemAdminService;
import com.thevip.global.response.ApiResponse;
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
public class CheeringItemAdminController {

    private final CheeringItemAdminService cheeringItemAdminService;

    @GetMapping("/api/v1/admin/cheering-items")
    public ApiResponse<List<CheeringItemAdminResponse>> list() {
        return ApiResponse.success(cheeringItemAdminService.list());
    }

    @GetMapping("/api/v1/admin/cheering-items/{itemId}")
    public ApiResponse<CheeringItemAdminResponse> get(@PathVariable Long itemId) {
        return ApiResponse.success(cheeringItemAdminService.get(itemId));
    }

    @PostMapping("/api/v1/admin/cheering-items")
    public ApiResponse<CheeringItemAdminResponse> create(@Valid @RequestBody CheeringItemAdminRequest request) {
        return ApiResponse.success(cheeringItemAdminService.create(request));
    }

    @PutMapping("/api/v1/admin/cheering-items/{itemId}")
    public ApiResponse<CheeringItemAdminResponse> update(@PathVariable Long itemId,
            @Valid @RequestBody CheeringItemAdminRequest request) {
        return ApiResponse.success(cheeringItemAdminService.update(itemId, request));
    }
}
