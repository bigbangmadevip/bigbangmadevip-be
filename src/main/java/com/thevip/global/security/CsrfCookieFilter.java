package com.thevip.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * CsrfToken은 기본적으로 지연(lazy) 계산이라, 아무도 값을 읽지 않으면 XSRF-TOKEN 쿠키 자체가 발급되지 않는다.
 * SPA 프론트가 로그인 후 조회(GET)만 하다가 처음 POST할 때 보낼 토큰이 없어 403이 나는 게 이 때문이다.
 * 모든 요청에서 토큰을 강제로 한 번 읽어(resolve) 쿠키가 항상 발급되게 한다.
 * (Spring Security 공식 문서의 SPA CSRF 연동 권장 패턴)
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
