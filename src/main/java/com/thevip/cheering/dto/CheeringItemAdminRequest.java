package com.thevip.cheering.dto;

import com.thevip.cheering.entity.CheeringCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheeringItemAdminRequest(
        @NotNull CheeringCategory category,
        @NotBlank @Size(max = 50) String title,
        @Size(max = 100) String subtitle,
        int sortOrder,
        boolean active) {
}
