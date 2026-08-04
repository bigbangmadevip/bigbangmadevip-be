package com.thevip.home.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.home.dto.HomeResponse;
import com.thevip.home.service.HomeService;
import com.thevip.member.entity.Member;
import com.thevip.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final MemberService memberService;

    @GetMapping("/api/v1/home")
    public ApiResponse<HomeResponse> home(OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        return ApiResponse.success(homeService.getHome(member.getId()));
    }
}
