package com.thevip.cheering.service;

import com.thevip.cheering.dto.ParticipateResponse;
import com.thevip.cheering.entity.Cheering;
import com.thevip.cheering.entity.CheeringItem;
import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.cheering.repository.CheeringRepository;
import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheeringService {

    private final CheeringRepository cheeringRepository;
    private final CheeringItemRepository cheeringItemRepository;

    @Transactional(readOnly = true)
    public List<Long> getCompletedItemIds(Long memberId) {
        return cheeringRepository.findItemIdsByMemberIdAndCheeringDate(memberId, LocalDate.now());
    }

    @Transactional
    public ParticipateResponse participate(Long memberId, Long itemId) {
        CheeringItem item = cheeringItemRepository.findById(itemId)
                .filter(CheeringItem::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 응원 항목입니다."));

        LocalDate today = LocalDate.now();
        boolean alreadyParticipated = cheeringRepository.existsByMemberIdAndItemIdAndCheeringDate(
                memberId, item.getId(), today);
        if (alreadyParticipated) {
            throw new BusinessException(ErrorCode.CHEERING_ALREADY_PARTICIPATED);
        }

        cheeringRepository.save(Cheering.of(memberId, item.getId()));

        // 캐싱하지 않는다: 방금 참여한 본인에게 보여줄 실시간 피드백이라, 홈 카운터와 달리 지연이 있으면 안 된다.
        long typeCompletedCount = cheeringRepository.countDistinctMemberByCategoryAndCheeringDate(
                item.getCategory(), today);
        return new ParticipateResponse(typeCompletedCount);
    }
}
