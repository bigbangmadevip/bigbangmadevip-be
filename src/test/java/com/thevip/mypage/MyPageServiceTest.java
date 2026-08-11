package com.thevip.mypage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.cheering.dto.CheeringCatalogItem;
import com.thevip.cheering.entity.CheeringCategory;
import com.thevip.cheering.entity.CheeringItem;
import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.cheering.repository.CheeringRepository;
import com.thevip.cheering.service.CheeringCatalogService;
import com.thevip.cheering.service.CheeringService;
import com.thevip.mypage.dto.CheeringCalendarResponse;
import com.thevip.mypage.dto.CheeringRecordDayResponse;
import com.thevip.mypage.dto.MyPageResponse;
import com.thevip.mypage.service.MyPageService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MyPageServiceTest {

    @Test
    void 마이페이지_응답은_오늘의_응원_현황과_누적_기록을_함께_담는다() {
        CheeringCatalogService cheeringCatalogService = mock(CheeringCatalogService.class);
        CheeringService cheeringService = mock(CheeringService.class);
        CheeringRepository cheeringRepository = mock(CheeringRepository.class);
        CheeringItemRepository cheeringItemRepository = mock(CheeringItemRepository.class);

        when(cheeringCatalogService.getActiveCatalog()).thenReturn(List.of(
                new CheeringCatalogItem(1L, CheeringCategory.STREAMING, "음원\n스트리밍", null),
                new CheeringCatalogItem(2L, CheeringCategory.VOTE, "투표", null),
                new CheeringCatalogItem(3L, CheeringCategory.VOTE, "투표2", null)));
        when(cheeringService.getCompletedItemIds(1L)).thenReturn(List.of(1L));
        when(cheeringRepository.countByMemberId(1L)).thenReturn(19L);
        when(cheeringRepository.countDistinctCheeringDateByMemberId(1L)).thenReturn(12L);
        when(cheeringRepository.countDistinctCheeringDateByMemberIdAndCheeringDateBetween(anyLong(), any(), any()))
                .thenReturn(10L);
        when(cheeringRepository.findFirstCheeringDateByMemberId(1L)).thenReturn(LocalDate.of(2026, 8, 19));

        MyPageService service = new MyPageService(
                cheeringCatalogService, cheeringService, cheeringRepository, cheeringItemRepository);
        MyPageResponse result = service.getMyPage(1L);

        assertThat(result.todayCheering().completedCount()).isEqualTo(1);
        assertThat(result.todayCheering().totalCount()).isEqualTo(3);
        assertThat(result.cheeringRecord().totalParticipationCount()).isEqualTo(19);
        assertThat(result.cheeringRecord().participatedDayCount()).isEqualTo(12);
        assertThat(result.cheeringRecord().participatedDayCountThisMonth()).isEqualTo(10);
        assertThat(result.cheeringRecord().firstParticipatedDate()).isEqualTo(LocalDate.of(2026, 8, 19));
    }

    @Test
    void 참여_기록이_없으면_첫_응원일은_null이다() {
        CheeringCatalogService cheeringCatalogService = mock(CheeringCatalogService.class);
        CheeringService cheeringService = mock(CheeringService.class);
        CheeringRepository cheeringRepository = mock(CheeringRepository.class);
        CheeringItemRepository cheeringItemRepository = mock(CheeringItemRepository.class);

        when(cheeringCatalogService.getActiveCatalog()).thenReturn(List.of());
        when(cheeringService.getCompletedItemIds(1L)).thenReturn(List.of());
        when(cheeringRepository.findFirstCheeringDateByMemberId(1L)).thenReturn(null);

        MyPageService service = new MyPageService(
                cheeringCatalogService, cheeringService, cheeringRepository, cheeringItemRepository);
        MyPageResponse result = service.getMyPage(1L);

        assertThat(result.cheeringRecord().firstParticipatedDate()).isNull();
    }

    @Test
    void 응원_기록_캘린더는_해당_월에_참여한_날짜_목록을_반환한다() {
        CheeringCatalogService cheeringCatalogService = mock(CheeringCatalogService.class);
        CheeringService cheeringService = mock(CheeringService.class);
        CheeringRepository cheeringRepository = mock(CheeringRepository.class);
        CheeringItemRepository cheeringItemRepository = mock(CheeringItemRepository.class);

        List<LocalDate> dates = List.of(LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 24));
        when(cheeringRepository.findDistinctCheeringDateByMemberIdAndCheeringDateBetween(
                1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))).thenReturn(dates);

        MyPageService service = new MyPageService(
                cheeringCatalogService, cheeringService, cheeringRepository, cheeringItemRepository);
        CheeringCalendarResponse result = service.getCheeringCalendar(1L, YearMonth.of(2026, 8));

        assertThat(result.yearMonth()).isEqualTo(YearMonth.of(2026, 8));
        assertThat(result.participatedDates()).containsExactly(
                LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 24));
    }

    @Test
    void 응원_기록_상세는_참여한_항목을_카탈로그_순서로_정렬해서_반환한다() {
        CheeringCatalogService cheeringCatalogService = mock(CheeringCatalogService.class);
        CheeringService cheeringService = mock(CheeringService.class);
        CheeringRepository cheeringRepository = mock(CheeringRepository.class);
        CheeringItemRepository cheeringItemRepository = mock(CheeringItemRepository.class);

        LocalDate date = LocalDate.of(2026, 8, 20);
        when(cheeringRepository.findItemIdsByMemberIdAndCheeringDate(1L, date)).thenReturn(List.of(2L, 1L));

        CheeringItem second = CheeringItem.of(CheeringCategory.VOTE, "인기가요 사전 투표", null, 1);
        ReflectionTestUtils.setField(second, "id", 2L);
        CheeringItem first = CheeringItem.of(CheeringCategory.STREAMING, "음원 스트리밍", null, 0);
        ReflectionTestUtils.setField(first, "id", 1L);
        when(cheeringItemRepository.findAllById(List.of(2L, 1L))).thenReturn(List.of(second, first));

        MyPageService service = new MyPageService(
                cheeringCatalogService, cheeringService, cheeringRepository, cheeringItemRepository);
        CheeringRecordDayResponse result = service.getCheeringRecordDay(1L, date);

        assertThat(result.completedCount()).isEqualTo(2);
        assertThat(result.items()).extracting("title").containsExactly("음원 스트리밍", "인기가요 사전 투표");
    }
}
