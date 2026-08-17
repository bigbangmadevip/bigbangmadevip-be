package com.thevip.cheering.service;

import com.thevip.cheering.dto.CheeringItemAdminRequest;
import com.thevip.cheering.dto.CheeringItemAdminResponse;
import com.thevip.cheering.entity.CheeringItem;
import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheeringItemAdminService {

    private final CheeringItemRepository cheeringItemRepository;

    @Transactional(readOnly = true)
    public List<CheeringItemAdminResponse> list() {
        return cheeringItemRepository.findAll().stream()
                .sorted(Comparator.comparingInt(CheeringItem::getSortOrder))
                .map(CheeringItemAdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CheeringItemAdminResponse get(Long id) {
        return CheeringItemAdminResponse.from(getEntity(id));
    }

    @Transactional
    public CheeringItemAdminResponse create(CheeringItemAdminRequest request) {
        CheeringItem item = CheeringItem.of(request.category(), request.title(), request.subtitle(),
                request.sortOrder());
        item.updateActive(request.active());
        cheeringItemRepository.save(item);
        return CheeringItemAdminResponse.from(item);
    }

    @Transactional
    public CheeringItemAdminResponse update(Long id, CheeringItemAdminRequest request) {
        CheeringItem item = getEntity(id);
        item.update(request.category(), request.title(), request.subtitle(), request.sortOrder());
        item.updateActive(request.active());
        return CheeringItemAdminResponse.from(item);
    }

    private CheeringItem getEntity(Long id) {
        return cheeringItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 응원 항목입니다."));
    }
}
