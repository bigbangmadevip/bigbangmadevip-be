package com.thevip.member.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.member.dto.RoleRequestCreateRequest;
import com.thevip.member.dto.RoleRequestListItemResponse;
import com.thevip.member.dto.RoleRequestResponse;
import com.thevip.member.entity.Member;
import com.thevip.member.service.MemberRoleRequestService;
import com.thevip.member.service.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberRoleRequestController {

    private final MemberService memberService;
    private final MemberRoleRequestService memberRoleRequestService;

    @PostMapping("/api/v1/admin/role-requests")
    public ApiResponse<RoleRequestResponse> submit(
            @Valid @RequestBody RoleRequestCreateRequest request, OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        return ApiResponse.success(memberRoleRequestService.submit(member.getId(), request.requestedRole()));
    }

    @GetMapping("/api/v1/admin/role-requests/me")
    public ApiResponse<RoleRequestResponse> mine(OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        return ApiResponse.success(memberRoleRequestService.getMine(member.getId()));
    }

    @GetMapping("/api/v1/admin/role-requests")
    public ApiResponse<List<RoleRequestListItemResponse>> pending(OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        return ApiResponse.success(memberRoleRequestService.listPending(member.getRole()));
    }

    @PostMapping("/api/v1/admin/role-requests/{requestId}/approve")
    public ApiResponse<Void> approve(@PathVariable Long requestId, OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        memberRoleRequestService.approve(requestId, member.getId(), member.getRole());
        return ApiResponse.success();
    }

    @PostMapping("/api/v1/admin/role-requests/{requestId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long requestId, OAuth2AuthenticationToken authentication) {
        Member member = memberService.getCurrentMember(authentication);
        memberRoleRequestService.reject(requestId, member.getId(), member.getRole());
        return ApiResponse.success();
    }
}
