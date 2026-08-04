package com.thevip.cheering.dto;

import com.thevip.cheering.entity.CheeringCategory;

/**
 * 완료 여부와 무관한, 캐시 대상 카탈로그 항목.
 * 완료 여부는 유저별로 달라 캐시에 넣지 않고 요청 시점에 조립한다.
 */
public record CheeringCatalogItem(Long id, CheeringCategory category, String title, String subtitle) {
}
