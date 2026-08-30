package com.thevip.member.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.member.dto.MemberResponse;
import com.thevip.member.dto.UpdateFcmTokenRequest;
import com.thevip.member.dto.UpdateNicknameRequest;
import com.thevip.member.dto.UpdatePushSettingsRequest;
import com.thevip.member.entity.Member;
import com.thevip.member.service.MemberService;
import com.thevip.push.PushTopic;
import com.thevip.push.service.PushNotificationService;
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
    private final PushNotificationService pushNotificationService;

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

    // 알림 수신에 동의해서 FCM 토큰을 발급받으면 프론트가 이 API로 넘겨준다. 서버는 Member에 토큰을
    // 보관하고, 그 시점에 Member에 저장돼 있는 카테고리별 설정값 기준으로 해당 토픽들에 구독시킨다.
    @PatchMapping("/api/v1/me/fcm-token")
    public ApiResponse<Void> updateFcmToken(
            @Valid @RequestBody UpdateFcmTokenRequest request, OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        memberService.updateFcmToken(member.getId(), request.fcmToken());
        applyTopicSubscription(request.fcmToken(), PushTopic.URGENT, member.isUrgentPushEnabled());
        applyTopicSubscription(request.fcmToken(), PushTopic.MUSIC, member.isMusicPushEnabled());
        applyTopicSubscription(request.fcmToken(), PushTopic.VOTE, member.isVotePushEnabled());
        return ApiResponse.success();
    }

    // 알림 설정 화면(전체/긴급/음원/투표)에서 카테고리별 토글을 저장한다. "전체" 토글은 프론트가
    // 3개 값을 한 번에 같은 값으로 보내는 방식으로 처리하고, 서버는 별도 필드로 두지 않는다.
    @PatchMapping("/api/v1/me/push-settings")
    public ApiResponse<MemberResponse> updatePushSettings(
            @Valid @RequestBody UpdatePushSettingsRequest request, OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        Member updated = memberService.updatePushSettings(
                member.getId(), request.urgentPushEnabled(), request.musicPushEnabled(), request.votePushEnabled());
        applyTopicSubscription(updated.getFcmToken(), PushTopic.URGENT, request.urgentPushEnabled());
        applyTopicSubscription(updated.getFcmToken(), PushTopic.MUSIC, request.musicPushEnabled());
        applyTopicSubscription(updated.getFcmToken(), PushTopic.VOTE, request.votePushEnabled());
        return ApiResponse.success(MemberResponse.from(updated));
    }

    private void applyTopicSubscription(String fcmToken, PushTopic topic, boolean enabled) {
        if (fcmToken == null) {
            return;
        }
        if (enabled) {
            pushNotificationService.subscribe(fcmToken, topic);
        } else {
            pushNotificationService.unsubscribe(fcmToken, topic);
        }
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
