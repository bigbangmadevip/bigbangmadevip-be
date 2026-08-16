package com.thevip.global.security;

import tools.jackson.databind.ObjectMapper;
import com.thevip.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * REST API + SPA 프론트 구조라 로그아웃 기본 동작(302 리다이렉트)이 맞지 않는다.
 * 200 + 공통 응답 JSON으로 대체한다 (RestAuthenticationEntryPoint와 동일한 이유).
 */
@Component
@RequiredArgsConstructor
public class RestLogoutSuccessHandler implements LogoutSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.success()));
    }
}
