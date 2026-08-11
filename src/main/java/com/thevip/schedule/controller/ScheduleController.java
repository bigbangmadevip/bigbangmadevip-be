package com.thevip.schedule.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.schedule.dto.ScheduleCategory;
import com.thevip.schedule.dto.ScheduleDayResponse;
import com.thevip.schedule.dto.ScheduleMonthResponse;
import com.thevip.schedule.dto.VoteDisplayMode;
import com.thevip.schedule.service.ScheduleService;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/api/v1/schedule/months/{yearMonth}")
    public ApiResponse<ScheduleMonthResponse> month(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(defaultValue = "ALL") ScheduleCategory category,
            @RequestParam(defaultValue = "EVERY_DAY") VoteDisplayMode voteDisplay) {
        return ApiResponse.success(scheduleService.getMonth(yearMonth, category, voteDisplay));
    }

    @GetMapping("/api/v1/schedule/days/{date}")
    public ApiResponse<ScheduleDayResponse> day(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "ALL") ScheduleCategory category,
            @RequestParam(defaultValue = "EVERY_DAY") VoteDisplayMode voteDisplay) {
        return ApiResponse.success(scheduleService.getDay(date, category, voteDisplay));
    }
}
