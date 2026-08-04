package com.thevip.cheering.dto;

import com.thevip.cheering.entity.CheeringCategory;

public record CheeringItemResponse(String id, CheeringCategory category, String title, String subtitle,
        boolean completed) {

    public static CheeringItemResponse from(CheeringCatalogItem item, boolean completed) {
        return new CheeringItemResponse(item.id().toString(), item.category(), item.title(), item.subtitle(),
                completed);
    }
}
