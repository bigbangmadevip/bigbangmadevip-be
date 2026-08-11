package com.thevip.mypage.dto;

import java.time.LocalDate;
import java.util.List;

public record CheeringRecordDayResponse(LocalDate date, int completedCount, List<CheeringRecordItemResponse> items) {
}
