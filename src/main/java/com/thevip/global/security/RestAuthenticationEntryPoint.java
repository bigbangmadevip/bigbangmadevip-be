package com.thevip.global.security;

import tools.jackson.databind.ObjectMapper;
import com.thevip.global.exception.ErrorCode;
import com.thevip.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * REST API + SPA 프론트 구조에서는 인증 안 된 요청에 HTML 로그인 페이지로 302 리다이렉트하는
 * 기본 동작이 맞지 않는다. fetch/axios가 리다이렉트를 따라가버려 200+HTML을 받게 되어
 * 프론트가 "로그인 필요" 상태를 분기할 수 없기 때문에, 401 + 공통 에러 JSON으로 대체한다.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponse.error(errorCode.getCode(), errorCode.getMessage())));
    }
}
