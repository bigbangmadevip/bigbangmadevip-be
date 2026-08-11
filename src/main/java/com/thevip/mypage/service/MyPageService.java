package com.thevip.mypage.service;

import com.thevip.cheering.entity.CheeringItem;
import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.cheering.repository.CheeringRepository;
import com.thevip.cheering.service.CheeringCatalogService;
import com.thevip.cheering.service.CheeringService;
import com.thevip.mypage.dto.CheeringCalendarResponse;
import com.thevip.mypage.dto.CheeringRecordDayResponse;
import com.thevip.mypage.dto.CheeringRecordItemResponse;
import com.thevip.mypage.dto.CheeringRecordSummaryResponse;
import com.thevip.mypage.dto.MyPageResponse;
import com.thevip.mypage.dto.TodayCheeringResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final CheeringCatalogService cheeringCatalogService;
    private final CheeringService cheeringService;
    private final CheeringRepository cheeringRepository;
    private final CheeringItemRepository cheeringItemRepository;

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long memberId) {
        int totalCount = cheeringCatalogService.getActiveCatalog().size();
        int completedCount = cheeringService.getCompletedItemIds(memberId).size();
        TodayCheeringResponse todayCheering = new TodayCheeringResponse(completedCount, totalCount);

        LocalDate today = LocalDate.now();
        long totalParticipationCount = cheeringRepository.countByMemberId(memberId);
        long participatedDayCount = cheeringRepository.countDistinctCheeringDateByMemberId(memberId);
        long participatedDayCountThisMonth = cheeringRepository.countDistinctCheeringDateByMemberIdAndCheeringDateBetween(
                memberId, today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()));
        LocalDate firstParticipatedDate = cheeringRepository.findFirstCheeringDateByMemberId(memberId);
        CheeringRecordSummaryResponse cheeringRecord = new CheeringRecordSummaryResponse(
                totalParticipationCount, participatedDayCount, participatedDayCountThisMonth, firstParticipatedDate);

        return new MyPageResponse(todayCheering, cheeringRecord);
    }

    @Transactional(readOnly = true)
    public CheeringCalendarResponse getCheeringCalendar(Long memberId, YearMonth yearMonth) {
        List<LocalDate> dates = cheeringRepository.findDistinctCheeringDateByMemberIdAndCheeringDateBetween(
                memberId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
        return new CheeringCalendarResponse(yearMonth, dates);
    }

    @Transactional(readOnly = true)
    public CheeringRecordDayResponse getCheeringRecordDay(Long memberId, LocalDate date) {
        List<Long> itemIds = cheeringRepository.findItemIdsByMemberIdAndCheeringDate(memberId, date);
        List<CheeringRecordItemResponse> items = cheeringItemRepository.findAllById(itemIds).stream()
                .sorted(Comparator.comparingInt(CheeringItem::getSortOrder))
                .map(CheeringRecordItemResponse::from)
                .toList();
        return new CheeringRecordDayResponse(date, items.size(), items);
    }
}
