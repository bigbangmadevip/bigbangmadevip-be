package com.thevip.guide.dto;

import com.thevip.guide.entity.GuideType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record GuideAdminRequest(
        @NotNull GuideType guideType,
        Long platformId,
        @NotBlank @Size(max = 100) String title,
        List<String> imageUrls,
        boolean active,
        int sortOrder) {
}
