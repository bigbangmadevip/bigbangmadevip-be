package com.thevip.cheering.service;

import com.thevip.cheering.dto.CheeringCatalogItem;
import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.global.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheeringCatalogService {

    private final CheeringItemRepository cheeringItemRepository;

    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CHEERING_CATALOG)
    public List<CheeringCatalogItem> getActiveCatalog() {
        return cheeringItemRepository.findByActiveTrueOrderBySortOrder().stream()
                .map(item -> new CheeringCatalogItem(item.getId(), item.getCategory(), item.getTitle(),
                        item.getSubtitle()))
                .toList();
    }
}
