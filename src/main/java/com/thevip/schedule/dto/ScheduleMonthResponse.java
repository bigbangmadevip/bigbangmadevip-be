package com.thevip.schedule.dto;

import java.util.List;

public record ScheduleMonthResponse(List<ScheduleDayCountResponse> days) {
}
