package com.thevip.mypage.dto;

import java.time.LocalDate;

public record CheeringRecordSummaryResponse(
        long totalParticipationCount,
        long participatedDayCount,
        long participatedDayCountThisMonth,
        LocalDate firstParticipatedDate) {
}
