package com.thevip.member.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.member.dto.MemberResponse;
import com.thevip.member.dto.UpdateNicknameRequest;
import com.thevip.member.entity.Member;
import com.thevip.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/api/v1/me")
    public ApiResponse<MemberResponse> me(OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        return ApiResponse.success(MemberResponse.from(member));
    }

    @PatchMapping("/api/v1/me")
    public ApiResponse<MemberResponse> updateNickname(
            @Valid @RequestBody UpdateNicknameRequest request, OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        Member updated = memberService.updateNickname(member.getId(), request.nickname());
        return ApiResponse.success(MemberResponse.from(updated));
    }

    @PostMapping("/api/v1/me/terms-agreement")
    public ApiResponse<MemberResponse> agreeToTerms(OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        Member updated = memberService.agreeToTerms(member.getId());
        return ApiResponse.success(MemberResponse.from(updated));
    }

    @DeleteMapping("/api/v1/me")
    public ApiResponse<Void> withdraw(OAuth2AuthenticationToken authentication,
            HttpServletRequest request, HttpServletResponse response) {
        Member member = memberService.getCurrentMember(authentication);
        memberService.withdraw(member.getId());
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        new CookieClearingLogoutHandler("JSESSIONID").logout(request, response, authentication);
        return ApiResponse.success();
    }
}
