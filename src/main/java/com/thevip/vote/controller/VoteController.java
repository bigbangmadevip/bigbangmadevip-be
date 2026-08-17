package com.thevip.vote.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.notice.dto.NoticeDetailResponse;
import com.thevip.notice.dto.NoticeListItemResponse;
import com.thevip.notice.entity.NoticeMenuType;
import com.thevip.notice.service.NoticeService;
import com.thevip.vote.dto.VoteDetailResponse;
import com.thevip.vote.dto.VoteScheduleDetailResponse;
import com.thevip.vote.dto.VoteScheduleListItemResponse;
import com.thevip.vote.dto.VoteTodayResponse;
import com.thevip.vote.service.VoteDetailService;
import com.thevip.vote.service.VoteScheduleService;
import com.thevip.vote.service.VoteTodayService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VoteController {

    private final VoteTodayService voteTodayService;
    private final VoteDetailService voteDetailService;
    private final VoteScheduleService voteScheduleService;
    private final NoticeService noticeService;

    @GetMapping("/api/v1/vote/today")
    public ApiResponse<VoteTodayResponse> today() {
        return ApiResponse.success(voteTodayService.getToday());
    }

    @GetMapping("/api/v1/vote/detail/{detailId}")
    public ApiResponse<VoteDetailResponse> detail(@PathVariable Long detailId) {
        return ApiResponse.success(voteDetailService.getDetail(detailId));
    }

    @GetMapping("/api/v1/vote/schedules")
    public ApiResponse<List<VoteScheduleListItemResponse>> schedules() {
        return ApiResponse.success(voteScheduleService.getSchedules());
    }

    @GetMapping("/api/v1/vote/schedules/{musicShowId}")
    public ApiResponse<VoteScheduleDetailResponse> schedule(@PathVariable Long musicShowId) {
        return ApiResponse.success(voteScheduleService.getSchedule(musicShowId));
    }

    @GetMapping("/api/v1/vote/notices")
    public ApiResponse<List<NoticeListItemResponse>> notices() {
        return ApiResponse.success(noticeService.getNotices(NoticeMenuType.VOTE));
    }

    @GetMapping("/api/v1/vote/notices/{noticeId}")
    public ApiResponse<NoticeDetailResponse> notice(@PathVariable Long noticeId) {
        return ApiResponse.success(noticeService.getNotice(NoticeMenuType.VOTE, noticeId));
    }
}
