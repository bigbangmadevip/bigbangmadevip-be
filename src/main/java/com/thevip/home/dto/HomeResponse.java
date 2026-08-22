package com.thevip.home.dto;

import com.thevip.cheering.dto.CheeringItemResponse;
import java.util.List;

public record HomeResponse(
        long participantCount,
        List<HomeUrgentResponse> urgentDetails,
        List<HomeScheduleItemResponse> todaySchedule,
        long totalCheeringCount,
        long completedCheeringCount,
        List<CheeringItemResponse> cheeringItems) {
}
