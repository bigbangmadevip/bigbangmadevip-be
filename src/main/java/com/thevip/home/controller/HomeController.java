package com.thevip.home.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.home.dto.HomeResponse;
import com.thevip.home.service.HomeService;
import com.thevip.member.entity.Member;
import com.thevip.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final MemberService memberService;

    // 비로그인 사용자도 조회 가능 - 로그인 상태일 때만 응원 완료 여부를 반영한다.
    @GetMapping("/api/v1/home")
    public ApiResponse<HomeResponse> home() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = null;
        if (authentication instanceof OAuth2AuthenticationToken oauth) {
            Member member = memberService.getCurrentMember(oauth);
            memberId = member.getId();
        }
        return ApiResponse.success(homeService.getHome(memberId));
    }
}
