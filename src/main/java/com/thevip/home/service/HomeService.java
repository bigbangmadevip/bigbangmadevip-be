package com.thevip.home.service;

import com.thevip.cheering.dto.CheeringCatalogItem;
import com.thevip.cheering.dto.CheeringItemResponse;
import com.thevip.cheering.service.CheeringCatalogService;
import com.thevip.cheering.service.CheeringService;
import com.thevip.cheering.service.CheeringStatsService;
import com.thevip.home.dto.HomeResponse;
import com.thevip.home.dto.HomeScheduleItemResponse;
import com.thevip.home.dto.HomeUrgentResponse;
import com.thevip.streaming.service.BiigStreamCountService;
import com.thevip.youtube.service.YoutubeViewCountService;
import java.time.LocalDateTime;
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
    private final YoutubeViewCountService youtubeViewCountService;
    private final BiigStreamCountService biigStreamCountService;

    public HomeResponse getHome(Long memberId) {
        long participantCount = cheeringStatsService.getTodayParticipantCount();
        List<HomeUrgentResponse> urgentDetails = homeUrgentService.getCurrentUrgent();
        List<HomeScheduleItemResponse> todaySchedule = homeTodayScheduleService.getTodaySchedule();
        List<CheeringCatalogItem> catalog = cheeringCatalogService.getActiveCatalog();
        Set<Long> completedItemIds = memberId == null
                ? Set.of()
                : new HashSet<>(cheeringService.getCompletedItemIds(memberId));

        List<CheeringItemResponse> items = catalog.stream()
                .map(item -> CheeringItemResponse.from(item, completedItemIds.contains(item.id())))
                .toList();
        long completedCheeringCount = items.stream().filter(CheeringItemResponse::completed).count();
        Long youtubeViewCount = youtubeViewCountService.getViewCount();
        Long biigStreamCount = biigStreamCountService.getCount();
        // youtubeViewCount/biigStreamCount 둘 다 매시 정각에 갱신되는 값이라 기준 시각은 하나로
        // 묶어서 내려준다. biigStreamCount는 외부 API 호출 없이 항상 갱신에 성공하므로 이 값을 쓴다.
        LocalDateTime statsUpdatedAt = biigStreamCountService.getUpdatedAt();

        return new HomeResponse(
                participantCount, urgentDetails, todaySchedule, items.size(), completedCheeringCount, items,
                youtubeViewCount, biigStreamCount, statsUpdatedAt);
    }
}
