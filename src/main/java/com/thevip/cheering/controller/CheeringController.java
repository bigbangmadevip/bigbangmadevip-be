package com.thevip.cheering.controller;

import com.thevip.cheering.dto.ParticipateResponse;
import com.thevip.cheering.service.CheeringService;
import com.thevip.global.response.ApiResponse;
import com.thevip.member.entity.Member;
import com.thevip.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CheeringController {

    private final CheeringService cheeringService;
    private final MemberService memberService;

    @PostMapping("/api/v1/cheerings/{itemId}")
    public ApiResponse<ParticipateResponse> participate(
            @PathVariable Long itemId, OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        return ApiResponse.success(cheeringService.participate(member.getId(), itemId));
    }
}
