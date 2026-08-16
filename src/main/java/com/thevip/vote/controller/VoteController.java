package com.thevip.vote.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.vote.dto.VoteTodayResponse;
import com.thevip.vote.service.VoteTodayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VoteController {

    private final VoteTodayService voteTodayService;

    @GetMapping("/api/v1/vote/today")
    public ApiResponse<VoteTodayResponse> today() {
        return ApiResponse.success(voteTodayService.getToday());
    }
}
