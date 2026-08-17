package com.thevip.cheering.dto;

import com.thevip.cheering.entity.CheeringItem;

public record CheeringItemAdminResponse(
        Long id,
        String category,
        String title,
        String subtitle,
        int sortOrder,
        boolean active) {

    public static CheeringItemAdminResponse from(CheeringItem item) {
        return new CheeringItemAdminResponse(
                item.getId(),
                item.getCategory().name(),
                item.getTitle(),
                item.getSubtitle(),
                item.getSortOrder(),
                item.isActive());
    }
}
