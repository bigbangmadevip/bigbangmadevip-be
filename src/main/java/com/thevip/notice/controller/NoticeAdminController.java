package com.thevip.notice.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.member.service.MemberService;
import com.thevip.notice.dto.NoticeAdminRequest;
import com.thevip.notice.dto.NoticeAdminResponse;
import com.thevip.notice.entity.NoticeMenuType;
import com.thevip.notice.service.NoticeAdminService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// 음원/투표 공지 어드민을 한 컨트롤러로 묶는다. menu 경로값은 SecurityConfig가
// /api/v1/admin/music/**, /api/v1/admin/vote/** 로 이미 역할을 나눠 차단하므로
// 여기서 별도 권한 분기는 필요 없다.
@RestController
@RequiredArgsConstructor
public class NoticeAdminController {

    private final NoticeAdminService noticeAdminService;
    private final MemberService memberService;

    @GetMapping("/api/v1/admin/{menu:music|vote}/notices")
    public ApiResponse<List<NoticeAdminResponse>> list(@PathVariable String menu) {
        return ApiResponse.success(noticeAdminService.list(menuType(menu)));
    }

    @GetMapping("/api/v1/admin/{menu:music|vote}/notices/{noticeId}")
    public ApiResponse<NoticeAdminResponse> get(@PathVariable String menu, @PathVariable Long noticeId) {
        return ApiResponse.success(noticeAdminService.get(menuType(menu), noticeId));
    }

    @PostMapping("/api/v1/admin/{menu:music|vote}/notices")
    public ApiResponse<NoticeAdminResponse> create(@PathVariable String menu,
            @Valid @RequestBody NoticeAdminRequest request, OAuth2AuthenticationToken authentication) {
        return ApiResponse.success(noticeAdminService.create(menuType(menu), request, adminName(authentication)));
    }

    @PutMapping("/api/v1/admin/{menu:music|vote}/notices/{noticeId}")
    public ApiResponse<NoticeAdminResponse> update(@PathVariable String menu, @PathVariable Long noticeId,
            @Valid @RequestBody NoticeAdminRequest request, OAuth2AuthenticationToken authentication) {
        return ApiResponse.success(
                noticeAdminService.update(menuType(menu), noticeId, request, adminName(authentication)));
    }

    private NoticeMenuType menuType(String menu) {
        return NoticeMenuType.valueOf(menu.toUpperCase());
    }

    private String adminName(OAuth2AuthenticationToken authentication) {
        return memberService.getCurrentMember(authentication).getNickname();
    }
}
