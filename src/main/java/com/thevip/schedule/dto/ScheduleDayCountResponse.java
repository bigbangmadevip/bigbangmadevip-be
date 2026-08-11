package com.thevip.schedule.dto;

import java.time.LocalDate;

public record ScheduleDayCountResponse(LocalDate date, int musicCount, int voteCount) {
}
