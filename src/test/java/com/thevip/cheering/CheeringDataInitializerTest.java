package com.thevip.cheering;

import com.thevip.cheering.entity.CheeringCategory;
import com.thevip.cheering.entity.CheeringItem;
import com.thevip.cheering.repository.CheeringItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
class CheeringDataInitializerTest {

    @Autowired
    CheeringItemRepository cheeringItemRepository;

    @Test
    void 기본_항목_8개가_sortOrder_순서로_시드된다() {
        List<CheeringItem> items = cheeringItemRepository.findByActiveTrueOrderBySortOrder();

        assertThat(items)
                .extracting(CheeringItem::getCategory, CheeringItem::getTitle, CheeringItem::getSubtitle,
                        CheeringItem::getSortOrder)
                .containsExactly(
                        tuple(CheeringCategory.STREAMING, "음원\n스트리밍", "봄여름가을겨울", 0),
                        tuple(CheeringCategory.VOTE, "인기가요\n사전 투표", null, 1),
                        tuple(CheeringCategory.VOTE, "멜론 주간인기상\n투표", null, 2),
                        tuple(CheeringCategory.YOUTUBE, "유튜브\n뮤직비디오 조회", "봄여름가을겨울", 3),
                        tuple(CheeringCategory.REPORT, "네이버 기사\n댓글 작성", null, 4),
                        tuple(CheeringCategory.DOWNLOAD, "선착순\n이벤트 참여", null, 5),
                        tuple(CheeringCategory.HASHTAG, "해시태그\n총공 이벤트", null, 6),
                        tuple(CheeringCategory.VOTECOIN, "어쩌구\n저쩌구", null, 7));
        assertThat(items).allMatch(CheeringItem::isActive);
    }
}
