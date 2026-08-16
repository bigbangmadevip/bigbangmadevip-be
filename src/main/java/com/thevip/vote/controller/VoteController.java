package com.thevip.vote.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.vote.dto.VoteDetailResponse;
import com.thevip.vote.dto.VoteTodayResponse;
import com.thevip.vote.service.VoteDetailService;
import com.thevip.vote.service.VoteTodayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VoteController {

    private final VoteTodayService voteTodayService;
    private final VoteDetailService voteDetailService;

    @GetMapping("/api/v1/vote/today")
    public ApiResponse<VoteTodayResponse> today() {
        return ApiResponse.success(voteTodayService.getToday());
    }

    @GetMapping("/api/v1/vote/detail/{detailId}")
    public ApiResponse<VoteDetailResponse> detail(@PathVariable Long detailId) {
        return ApiResponse.success(voteDetailService.getDetail(detailId));
    }
}
