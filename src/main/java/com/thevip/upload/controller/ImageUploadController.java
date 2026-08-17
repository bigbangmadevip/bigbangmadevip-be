package com.thevip.upload.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.upload.dto.ImageUploadResponse;
import com.thevip.upload.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @PostMapping("/api/v1/admin/images")
    public ApiResponse<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(new ImageUploadResponse(imageUploadService.upload(file)));
    }
}
