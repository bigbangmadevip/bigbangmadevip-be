package com.thevip.cheering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thevip.cheering.dto.ParticipateResponse;
import com.thevip.cheering.entity.CheeringItem;
import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.cheering.repository.CheeringRepository;
import com.thevip.cheering.service.CheeringService;
import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CheeringServiceTest {

    @Autowired
    CheeringService cheeringService;

    @Autowired
    CheeringItemRepository cheeringItemRepository;

    @Autowired
    CheeringRepository cheeringRepository;

    @Test
    void 참여하면_완료_목록에_포함된다() {
        Long memberId = 9001L;
        Long itemId = anyActiveItemId();

        cheeringService.participate(memberId, itemId);

        List<Long> completed = cheeringService.getCompletedItemIds(memberId);
        assertThat(completed).containsExactly(itemId);
    }

    @Test
    void 같은_항목을_같은날_두번_참여하면_거부된다() {
        Long memberId = 9002L;
        Long itemId = anyActiveItemId();
        cheeringService.participate(memberId, itemId);

        assertThatThrownBy(() -> cheeringService.participate(memberId, itemId))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CHEERING_ALREADY_PARTICIPATED));
    }

    @Test
    void 참여하면_해당_항목과_타입의_오늘_완료자_수가_1씩_증가한다() {
        Long itemId = anyActiveItemId();
        CheeringItem item = cheeringItemRepository.findById(itemId).orElseThrow();
        LocalDate today = LocalDate.now();

        long itemCountBefore = cheeringRepository.countDistinctMemberByItemIdAndCheeringDate(itemId, today);
        long typeCountBefore = cheeringRepository.countDistinctMemberByCategoryAndCheeringDate(
                item.getCategory(), today);

        // 다른 테스트와 DB를 공유하므로, 절대값이 아니라 이 참여로 인한 증분만 검증한다.
        long freshMemberId = System.nanoTime();
        ParticipateResponse response = cheeringService.participate(freshMemberId, itemId);

        assertThat(response.itemCompletedCount()).isEqualTo(itemCountBefore + 1);
        assertThat(response.typeCompletedCount()).isEqualTo(typeCountBefore + 1);
    }

    @Test
    void 존재하지_않는_항목은_참여가_거부된다() {
        assertThatThrownBy(() -> cheeringService.participate(9003L, 999_999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_FOUND));
    }

    private Long anyActiveItemId() {
        return cheeringItemRepository.findByActiveTrueOrderBySortOrder().stream()
                .findFirst()
                .map(CheeringItem::getId)
                .orElseThrow();
    }
}
