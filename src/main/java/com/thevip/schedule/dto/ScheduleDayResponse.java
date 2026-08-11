package com.thevip.schedule.dto;

import java.time.LocalDate;
import java.util.List;

public record ScheduleDayResponse(LocalDate date, List<ScheduleItemResponse> items) {
}
