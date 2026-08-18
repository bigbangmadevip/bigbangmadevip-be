package com.thevip.cheering.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheeringItemAdminRequest(
        @NotBlank @Size(max = 20) String category,
        @NotBlank @Size(max = 50) String title,
        @Size(max = 100) String subtitle,
        int sortOrder,
        boolean active) {
}
