package com.thevip.home.service;

import com.thevip.cheering.dto.CheeringCatalogItem;
import com.thevip.cheering.dto.CheeringItemResponse;
import com.thevip.cheering.service.CheeringCatalogService;
import com.thevip.cheering.service.CheeringService;
import com.thevip.cheering.service.CheeringStatsService;
import com.thevip.home.dto.HomeResponse;
import com.thevip.home.dto.HomeScheduleItemResponse;
import com.thevip.home.dto.HomeUrgentResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final CheeringStatsService cheeringStatsService;
    private final CheeringCatalogService cheeringCatalogService;
    private final CheeringService cheeringService;
    private final HomeUrgentService homeUrgentService;
    private final HomeTodayScheduleService homeTodayScheduleService;

    public HomeResponse getHome(Long memberId) {
        long participantCount = cheeringStatsService.getTodayParticipantCount();
        List<HomeUrgentResponse> urgentDetails = homeUrgentService.getCurrentUrgent();
        List<HomeScheduleItemResponse> todaySchedule = homeTodayScheduleService.getTodaySchedule();
        List<CheeringCatalogItem> catalog = cheeringCatalogService.getActiveCatalog();
        Set<Long> completedItemIds = new HashSet<>(cheeringService.getCompletedItemIds(memberId));

        List<CheeringItemResponse> items = catalog.stream()
                .map(item -> CheeringItemResponse.from(item, completedItemIds.contains(item.id())))
                .toList();
        long completedCheeringCount = items.stream().filter(CheeringItemResponse::completed).count();

        return new HomeResponse(
                participantCount, urgentDetails, todaySchedule, items.size(), completedCheeringCount, items);
    }
}
