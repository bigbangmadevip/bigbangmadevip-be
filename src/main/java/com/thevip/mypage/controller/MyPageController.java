package com.thevip.mypage.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.member.entity.Member;
import com.thevip.member.service.MemberService;
import com.thevip.mypage.dto.CheeringCalendarResponse;
import com.thevip.mypage.dto.CheeringRecordDayResponse;
import com.thevip.mypage.dto.MyPageResponse;
import com.thevip.mypage.service.MyPageService;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;
    private final MemberService memberService;

    @GetMapping("/api/v1/mypage")
    public ApiResponse<MyPageResponse> myPage(OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        return ApiResponse.success(myPageService.getMyPage(member.getId()));
    }

    @GetMapping("/api/v1/mypage/cheering-calendar/{yearMonth}")
    public ApiResponse<CheeringCalendarResponse> cheeringCalendar(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        return ApiResponse.success(myPageService.getCheeringCalendar(member.getId(), yearMonth));
    }

    @GetMapping("/api/v1/mypage/cheering-records/{date}")
    public ApiResponse<CheeringRecordDayResponse> cheeringRecordDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        return ApiResponse.success(myPageService.getCheeringRecordDay(member.getId(), date));
    }
}
