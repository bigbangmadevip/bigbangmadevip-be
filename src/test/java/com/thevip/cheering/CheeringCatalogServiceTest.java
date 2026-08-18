package com.thevip.cheering;

import static org.assertj.core.api.Assertions.assertThat;

import com.thevip.cheering.dto.CheeringCatalogItem;
import com.thevip.cheering.service.CheeringCatalogService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CheeringCatalogServiceTest {

    @Autowired
    CheeringCatalogService cheeringCatalogService;

    @Test
    void 카탈로그는_sortOrder_순서로_정렬된다() {
        List<CheeringCatalogItem> catalog = cheeringCatalogService.getActiveCatalog();

        assertThat(catalog).extracting(CheeringCatalogItem::title)
                .containsExactly(
                        "음원\n스트리밍",
                        "인기가요\n사전 투표",
                        "멜론 주간인기상\n투표",
                        "유튜브\n뮤직비디오 조회",
                        "네이버 기사\n댓글 작성",
                        "선착순\n이벤트 참여",
                        "해시태그\n총공 이벤트",
                        "어쩌구\n저쩌구");
        assertThat(catalog).extracting(CheeringCatalogItem::category)
                .containsExactly(
                        "STREAMING",
                        "VOTE",
                        "VOTE",
                        "YOUTUBE",
                        "REPORT",
                        "DOWNLOAD",
                        "HASHTAG",
                        "VOTECOIN");
    }
}
