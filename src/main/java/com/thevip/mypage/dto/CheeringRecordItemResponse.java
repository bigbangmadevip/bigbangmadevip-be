package com.thevip.mypage.dto;

import com.thevip.cheering.entity.CheeringItem;

public record CheeringRecordItemResponse(String id, String title) {

    public static CheeringRecordItemResponse from(CheeringItem item) {
        return new CheeringRecordItemResponse(item.getId().toString(), item.getTitle());
    }
}
