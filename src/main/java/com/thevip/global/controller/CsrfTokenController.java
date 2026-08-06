package com.thevip.global.controller;

import com.thevip.global.response.ApiResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 프론트가 XSRF-TOKEN 쿠키를 못 읽는 cross-origin 환경(로컬 프론트 -> 배포된 백엔드)에서
 * CSRF 토큰을 응답 body로 대신 받아가기 위한 엔드포인트. 쿠키는 그대로 브라우저가 자동으로 실어 보내니
 * 이 값을 X-XSRF-TOKEN 헤더에 담아 쓰기 요청을 보내면 double-submit 검증을 통과한다.
 */
@RestController
public class CsrfTokenController {

    @GetMapping("/api/v1/csrf-token")
    public ApiResponse<CsrfTokenResponse> csrfToken(CsrfToken csrfToken) {
        return ApiResponse.success(new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken()));
    }

    private record CsrfTokenResponse(String headerName, String token) {
    }
}
