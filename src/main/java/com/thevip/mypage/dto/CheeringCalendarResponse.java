package com.thevip.mypage.dto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public record CheeringCalendarResponse(YearMonth yearMonth, List<LocalDate> participatedDates) {
}
